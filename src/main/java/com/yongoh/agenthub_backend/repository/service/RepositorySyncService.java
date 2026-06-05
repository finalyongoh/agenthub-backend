package com.yongoh.agenthub_backend.repository.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yongoh.agenthub_backend.github.GithubApiException;
import com.yongoh.agenthub_backend.github.GithubReadmeService;
import com.yongoh.agenthub_backend.github.GithubRepositorySearchService;
import com.yongoh.agenthub_backend.github.dto.GithubReadmeDto;
import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.global.config.GithubProperties;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryReadmeJpaRepository;

@Service
public class RepositorySyncService {
	private static final Logger log = LoggerFactory.getLogger(RepositorySyncService.class);

	private final GithubRepositorySearchService searchService;
	private final GithubReadmeService readmeService;
	private final AgentRepositoryScorer scorer;
	private final AgentCategoryClassifier classifier;
	private final ReadmeSummaryGenerator summaryGenerator;
	private final AgentRepositoryJpaRepository repositoryJpaRepository;
	private final RepositoryReadmeJpaRepository readmeJpaRepository;
	private final GithubProperties properties;

	public RepositorySyncService(
		GithubRepositorySearchService searchService,
		GithubReadmeService readmeService,
		AgentRepositoryScorer scorer,
		AgentCategoryClassifier classifier,
		ReadmeSummaryGenerator summaryGenerator,
		AgentRepositoryJpaRepository repositoryJpaRepository,
		RepositoryReadmeJpaRepository readmeJpaRepository,
		GithubProperties properties
	) {
		this.searchService = searchService;
		this.readmeService = readmeService;
		this.scorer = scorer;
		this.classifier = classifier;
		this.summaryGenerator = summaryGenerator;
		this.repositoryJpaRepository = repositoryJpaRepository;
		this.readmeJpaRepository = readmeJpaRepository;
		this.properties = properties;
	}

	@Transactional
	public List<AgentRepository> searchAndSaveCandidates(int limit, SyncStatistics statistics) {
		List<GithubRepositoryDto> candidates = searchService.searchAgentRepositories(limit);
		statistics.addSearchedCount(candidates.size());
		return candidates.stream()
			.map(candidate -> upsert(candidate, statistics))
			.toList();
	}

	@Transactional
	public void fetchReadmes(List<AgentRepository> repositories, boolean force, SyncStatistics statistics) {
		for (AgentRepository repository : repositories) {
			try {
				if (!force && readmeJpaRepository.findByRepository(repository).isPresent()) {
					statistics.incrementSkippedCount();
					continue;
				}
				readmeService.findReadme(repository)
					.ifPresentOrElse(readme -> saveReadme(repository, readme, statistics), statistics::incrementSkippedCount);
			} catch (GithubApiException exception) {
				if (exception.isAuthenticationError()) {
					throw exception;
				}
				log.warn("README fetch failed: repository={}, status={}", repository.getFullName(), exception.getStatusCode());
				statistics.incrementFailedCount();
			} catch (RuntimeException exception) {
				log.warn("README fetch failed: repository={}", repository.getFullName(), exception);
				statistics.incrementFailedCount();
			}
		}
	}

	@Transactional
	public void scoreRepositories(List<AgentRepository> repositories, SyncStatistics statistics) {
		for (AgentRepository repository : repositories) {
			readmeJpaRepository.findByRepository(repository).ifPresent(readme -> {
				int score = scorer.score(repository, readme.getContent());
				boolean agentRelated = scorer.isAgentRelated(score);
				String category = agentRelated ? classifier.classify(readme.getContent()) : null;
				String summary = summaryGenerator.generateReadmeSummary(readme.getContent(), repository.getDescription());
				repository.updateScoring(score, agentRelated, category, summary);
				if (agentRelated) {
					statistics.incrementAgentRelatedCount();
				}
			});
		}
	}

	private AgentRepository upsert(GithubRepositoryDto dto, SyncStatistics statistics) {
		AgentRepository repository = repositoryJpaRepository.findByGithubId(dto.githubId())
			.orElseGet(() -> AgentRepository.create(dto));
		repository.updateMetadata(dto);
		statistics.incrementSavedCount();
		return repositoryJpaRepository.save(repository);
	}

	private void saveReadme(AgentRepository repository, GithubReadmeDto readmeDto, SyncStatistics statistics) {
		RepositoryReadme readme = readmeJpaRepository.findByRepository(repository)
			.orElse(null);
		if (readme != null && readme.hasSameSha(readmeDto.sha())) {
			statistics.incrementSkippedCount();
			return;
		}
		String content = readmeDto.content();
		int originalLength = content == null ? 0 : content.length();
		boolean truncated = originalLength > properties.getSync().getReadmeMaxLength();
		String savedContent = truncated ? content.substring(0, properties.getSync().getReadmeMaxLength()) : content;
		if (readme == null) {
			readme = RepositoryReadme.create(repository, readmeDto.path(), readmeDto.sha(), savedContent, originalLength, truncated);
		} else {
			readme.update(readmeDto.path(), readmeDto.sha(), savedContent, originalLength, truncated);
		}
		readmeJpaRepository.save(readme);
		repository.markReadmeFetched();
		statistics.incrementReadmeFetchedCount();
	}
}
