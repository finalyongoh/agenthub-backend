package com.yongoh.agenthub_backend.repository.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yongoh.agenthub_backend.github.GithubApiException;
import com.yongoh.agenthub_backend.github.GithubFileTreeService;
import com.yongoh.agenthub_backend.github.GithubReadmeService;
import com.yongoh.agenthub_backend.github.GithubRepositorySearchService;
import com.yongoh.agenthub_backend.github.dto.GithubFileTreeItemDto;
import com.yongoh.agenthub_backend.github.dto.GithubReadmeDto;
import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.global.config.GithubProperties;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.model.RepositoryFileTree;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryFileTreeJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryReadmeJpaRepository;

@Service
public class RepositorySyncService {
	private static final Logger log = LoggerFactory.getLogger(RepositorySyncService.class);

	private final GithubRepositorySearchService searchService;
	private final GithubReadmeService readmeService;
	private final GithubFileTreeService fileTreeService;
	private final AgentRepositoryScorer scorer;
	private final AgentCategoryClassifier classifier;
	private final AgentTraceSummaryClient summaryClient;
	private final AgentRepositoryJpaRepository repositoryJpaRepository;
	private final RepositoryReadmeJpaRepository readmeJpaRepository;
	private final RepositoryFileTreeJpaRepository fileTreeJpaRepository;
	private final RepositoryAnalysisRepository analysisRepository;
	private final RepositoryNotificationService notificationService;
	private final GithubProperties properties;

	public RepositorySyncService(
		GithubRepositorySearchService searchService,
		GithubReadmeService readmeService,
		GithubFileTreeService fileTreeService,
		AgentRepositoryScorer scorer,
		AgentCategoryClassifier classifier,
		AgentTraceSummaryClient summaryClient,
		AgentRepositoryJpaRepository repositoryJpaRepository,
		RepositoryReadmeJpaRepository readmeJpaRepository,
		RepositoryFileTreeJpaRepository fileTreeJpaRepository,
		RepositoryAnalysisRepository analysisRepository,
		RepositoryNotificationService notificationService,
		GithubProperties properties
	) {
		this.searchService = searchService;
		this.readmeService = readmeService;
		this.fileTreeService = fileTreeService;
		this.scorer = scorer;
		this.classifier = classifier;
		this.summaryClient = summaryClient;
		this.repositoryJpaRepository = repositoryJpaRepository;
		this.readmeJpaRepository = readmeJpaRepository;
		this.fileTreeJpaRepository = fileTreeJpaRepository;
		this.analysisRepository = analysisRepository;
		this.notificationService = notificationService;
		this.properties = properties;
	}

	@Transactional
	public List<AgentRepository> searchAndSaveCandidates(int limit, SyncStatistics statistics) {
		List<GithubRepositoryDto> candidates = searchService.searchAgentRepositories(limit);
		statistics.addSearchedCount(candidates.size());
		Map<Long, AgentRepository> repositories = new LinkedHashMap<>();
		candidates.stream()
			.map(candidate -> upsert(candidate, statistics))
			.forEach(repository -> repositories.put(repository.getGithubId(), repository));
		repositoryJpaRepository.findAll(activeRefreshCandidate(), PageRequest.of(0, limit))
			.forEach(repository -> repositories.putIfAbsent(repository.getGithubId(), repository));
		return List.copyOf(repositories.values());
	}

	private Specification<AgentRepository> activeRefreshCandidate() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.and(
			criteriaBuilder.isFalse(root.get("archived")),
			criteriaBuilder.isFalse(root.get("fork")),
			criteriaBuilder.isTrue(root.get("agentRelated"))
		);
	}

	@Transactional
	public void fetchReadmes(List<AgentRepository> repositories, boolean force, SyncStatistics statistics) {
		for (AgentRepository repository : repositories) {
			try {
				readmeService.findReadme(repository)
					.ifPresentOrElse(readme -> {
						saveReadme(repository, readme, force, statistics);
						saveFileTree(repository);
						queueAnalysis(repository);
					}, statistics::incrementSkippedCount);
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
				boolean agentRelated = scorer.isAgentRelated(repository, readme.getContent());
				String category = agentRelated ? classifier.classify(readme.getContent()) : null;
				String summary = summarize(repository, readme, statistics);
				repository.updateScoring(score, agentRelated, category, summary);
				if (agentRelated) {
					statistics.incrementAgentRelatedCount();
				}
			});
		}
	}

	private String summarize(AgentRepository repository, RepositoryReadme readme, SyncStatistics statistics) {
		try {
			RepositoryFileTree fileTree = fileTreeJpaRepository.findByRepository(repository).orElse(null);
			AgentTraceSummaryClient.RepositorySummaryResult result = summaryClient.summarize(repository, readme, fileTree);
			if (result.completed()) {
				return result.readmeSummary();
			}
			log.warn("AgentTrace summary failed: repository={}, error={}", repository.getFullName(), result.errorMessage());
			statistics.incrementFailedCount();
			return repository.getReadmeSummary();
		} catch (AgentTraceSummaryException exception) {
			log.warn("AgentTrace summary request failed: repository={}", repository.getFullName(), exception);
			statistics.incrementFailedCount();
			return repository.getReadmeSummary();
		}
	}

	private AgentRepository upsert(GithubRepositoryDto dto, SyncStatistics statistics) {
		AgentRepository repository = repositoryJpaRepository.findByGithubId(dto.getGithubId())
			.orElseGet(() -> AgentRepository.create(dto));
		if (repository.getCreatedAt() != null) {
			notifyMetadataChanges(repository, dto);
		}
		repository.updateMetadata(dto);
		statistics.incrementSavedCount();
		return repositoryJpaRepository.save(repository);
	}

	private void saveReadme(AgentRepository repository, GithubReadmeDto readmeDto, boolean force, SyncStatistics statistics) {
		RepositoryReadme readme = readmeJpaRepository.findByRepository(repository)
			.orElse(null);
		if (!force && readme != null && readme.hasSameSha(readmeDto.getSha())) {
			statistics.incrementSkippedCount();
			return;
		}
		String oldSha = readme == null ? null : readme.getSha();
		String content = readmeDto.getContent();
		int originalLength = content == null ? 0 : content.length();
		boolean truncated = originalLength > properties.getSync().getReadmeMaxLength();
		String savedContent = truncated ? content.substring(0, properties.getSync().getReadmeMaxLength()) : content;
		if (readme == null) {
			readme = RepositoryReadme.create(repository, readmeDto.getPath(), readmeDto.getSha(), savedContent, originalLength, truncated);
		} else {
			readme.update(readmeDto.getPath(), readmeDto.getSha(), savedContent, originalLength, truncated);
		}
		readmeJpaRepository.save(readme);
		repository.markReadmeFetched();
		if (oldSha != null && readmeDto.getSha() != null && !oldSha.equals(readmeDto.getSha())) {
			notificationService.notifyChanged(repository, "readme_changed", "readmeSha", oldSha, readmeDto.getSha(), oldSha, readmeDto.getSha());
		}
		statistics.incrementReadmeFetchedCount();
	}

	private void saveFileTree(AgentRepository repository) {
		List<GithubFileTreeItemDto> fileTree = fileTreeService.findShallowFileTree(repository);
		String treeJson = toJson(fileTree);
		RepositoryFileTree snapshot = fileTreeJpaRepository.findByRepository(repository)
			.orElseGet(() -> RepositoryFileTree.create(repository, treeJson, fileTree.size()));
		snapshot.update(treeJson, fileTree.size());
		fileTreeJpaRepository.save(snapshot);
	}

	private void queueAnalysis(AgentRepository repository) {
		if (!analysisRepository.existsByRepositoryId(repository.getId())) {
			UUID fileTreeId = fileTreeJpaRepository.findByRepository(repository)
				.map(RepositoryFileTree::getId)
				.orElseGet(UUID::randomUUID);
			analysisRepository.save(RepositoryAnalysis.builder()
				.repositoryId(repository.getId())
				.snapshotId(fileTreeId)
				.status("QUEUED")
				.build());
		}
	}

	private String toJson(List<GithubFileTreeItemDto> fileTree) {
		StringBuilder builder = new StringBuilder("[");
		for (int index = 0; index < fileTree.size(); index++) {
			GithubFileTreeItemDto item = fileTree.get(index);
			if (index > 0) {
				builder.append(',');
			}
			builder.append("{\"path\":\"")
				.append(escapeJson(item.getPath()))
				.append("\",\"type\":\"")
				.append(escapeJson(item.getType()))
				.append("\"}");
		}
		return builder.append(']').toString();
	}

	private String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}

	private void notifyMetadataChanges(AgentRepository repository, GithubRepositoryDto dto) {
		notifyIfChanged(repository, "description", repository.getDescription(), dto.getDescription());
		notifyIfChanged(repository, "topics", repository.getTopics(), String.join(",", dto.getTopics()));
		notifyIfChanged(repository, "language", repository.getLanguage(), dto.getLanguage());
		notifyIfChanged(repository, "stars", String.valueOf(repository.getStars()), String.valueOf(dto.getStars()));
		notifyIfChanged(repository, "forks", String.valueOf(repository.getForks()), String.valueOf(dto.getForks()));
		notifyIfChanged(repository, "watchers", String.valueOf(repository.getWatchers()), String.valueOf(dto.getWatchers()));
		notifyIfChanged(repository, "openIssues", String.valueOf(repository.getOpenIssues()), String.valueOf(dto.getOpenIssues()));
		notifyIfChanged(repository, "license", repository.getLicense(), dto.getLicense());
		notifyIfChanged(repository, "pushedAt", String.valueOf(repository.getPushedAt()), String.valueOf(dto.getPushedAt()));
		notifyIfChanged(repository, "homepage", repository.getHomepage(), dto.getHomepage());
		notifyIfChanged(repository, "defaultBranch", repository.getDefaultBranch(), dto.getDefaultBranch());
	}

	private void notifyIfChanged(AgentRepository repository, String fieldName, String oldValue, String newValue) {
		if (!Objects.equals(normalize(oldValue), normalize(newValue))) {
			notificationService.notifyChanged(repository, "metadata_changed", fieldName, oldValue, newValue, null, null);
		}
	}

	private String normalize(String value) {
		return value == null ? "" : value;
	}
}
