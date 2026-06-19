package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.yongoh.agenthub_backend.github.GithubReadmeService;
import com.yongoh.agenthub_backend.github.GithubFileTreeService;
import com.yongoh.agenthub_backend.github.dto.GithubFileTreeItemDto;
import com.yongoh.agenthub_backend.github.dto.GithubReadmeDto;
import com.yongoh.agenthub_backend.github.GithubRepositorySearchService;
import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.global.config.GithubProperties;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.model.RepositoryFileTree;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryFileTreeJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryReadmeJpaRepository;

class RepositorySyncServiceTest {
	private final GithubRepositorySearchService searchService = mock(GithubRepositorySearchService.class);
	private final GithubReadmeService readmeService = mock(GithubReadmeService.class);
	private final GithubFileTreeService fileTreeService = mock(GithubFileTreeService.class);
	private final AgentRepositoryScorer scorer = mock(AgentRepositoryScorer.class);
	private final AgentCategoryClassifier classifier = mock(AgentCategoryClassifier.class);
	private final AgentTraceSummaryClient summaryClient = mock(AgentTraceSummaryClient.class);
	private final AgentRepositoryJpaRepository repositoryJpaRepository = mock(AgentRepositoryJpaRepository.class);
	private final RepositoryReadmeJpaRepository readmeJpaRepository = mock(RepositoryReadmeJpaRepository.class);
	private final RepositoryFileTreeJpaRepository fileTreeJpaRepository = mock(RepositoryFileTreeJpaRepository.class);
	private final RepositoryAnalysisRepository analysisRepository = mock(RepositoryAnalysisRepository.class);
	private final RepositoryNotificationService notificationService = mock(RepositoryNotificationService.class);
	private final GithubProperties properties = new GithubProperties();

	private final RepositorySyncService service = new RepositorySyncService(
		searchService,
		readmeService,
		fileTreeService,
		scorer,
		classifier,
		summaryClient,
		repositoryJpaRepository,
		readmeJpaRepository,
		fileTreeJpaRepository,
		analysisRepository,
		notificationService,
		properties
	);

	@Test
	void searchAndSaveCandidatesIncludesKnownRepositoriesForRefresh() {
		GithubRepositoryDto discoveredDto = repositoryDto(1L, "acme/new-agent");
		AgentRepository discovered = AgentRepository.create(discoveredDto);
		AgentRepository known = AgentRepository.create(repositoryDto(2L, "acme/known-agent"));

		when(searchService.searchAgentRepositories(10)).thenReturn(List.of(discoveredDto));
		when(repositoryJpaRepository.findByGithubId(1L)).thenReturn(Optional.empty());
		when(repositoryJpaRepository.save(any(AgentRepository.class))).thenReturn(discovered);
		when(repositoryJpaRepository.findAll(any(Specification.class), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of(known)));

		List<AgentRepository> repositories = service.searchAndSaveCandidates(10, new SyncStatistics());

		assertThat(repositories)
			.extracting(AgentRepository::getFullName)
			.containsExactly("acme/new-agent", "acme/known-agent");
	}

	@Test
	void fetchReadmesStoresShallowFileTreeSnapshot() {
		AgentRepository repository = AgentRepository.create(repositoryDto(3L, "acme/tree-agent"));
		when(readmeService.findReadme(repository))
			.thenReturn(Optional.of(new GithubReadmeDto("README.md", "readme-sha", "# Tree Agent")));
		when(readmeJpaRepository.findByRepository(repository)).thenReturn(Optional.empty());
		when(fileTreeService.findShallowFileTree(repository))
			.thenReturn(List.of(
				new GithubFileTreeItemDto("README.md", "file"),
				new GithubFileTreeItemDto("src/main.py", "file"),
				new GithubFileTreeItemDto("skills/skill.md", "file")
			));
		when(fileTreeJpaRepository.findByRepository(repository)).thenReturn(Optional.empty());

		service.fetchReadmes(List.of(repository), false, new SyncStatistics());

		verify(fileTreeJpaRepository).save(any(RepositoryFileTree.class));
	}

	@Test
	void fetchReadmesQueuesPendingAnalysisAfterSnapshot() {
		AgentRepository repository = AgentRepository.create(repositoryDto(4L, "acme/analysis-agent"));
		when(readmeService.findReadme(repository))
			.thenReturn(Optional.of(new GithubReadmeDto("README.md", "readme-sha", "# Analysis Agent")));
		when(readmeJpaRepository.findByRepository(repository)).thenReturn(Optional.empty());
		when(fileTreeService.findShallowFileTree(repository))
			.thenReturn(List.of(new GithubFileTreeItemDto("README.md", "file")));
		when(fileTreeJpaRepository.findByRepository(repository)).thenReturn(Optional.empty());
		when(analysisRepository.existsByRepositoryId(repository.getId())).thenReturn(false);

		service.fetchReadmes(List.of(repository), false, new SyncStatistics());

		verify(analysisRepository).save(any(RepositoryAnalysis.class));
	}

	@Test
	void scoreRepositoriesStoresAgentTraceReadmeSummary() {
		AgentRepository repository = AgentRepository.create(repositoryDto(5L, "acme/summary-agent"));
		var readme = com.yongoh.agenthub_backend.repository.model.RepositoryReadme.create(
			repository,
			"README.md",
			"readme-sha",
			"# Summary Agent",
			15,
			false
		);
		RepositoryFileTree fileTree = RepositoryFileTree.create(repository, "[{\"path\":\"README.md\",\"type\":\"file\"}]", 1);
		when(readmeJpaRepository.findByRepository(repository)).thenReturn(Optional.of(readme));
		when(fileTreeJpaRepository.findByRepository(repository)).thenReturn(Optional.of(fileTree));
		when(scorer.score(repository, readme.getContent())).thenReturn(9);
		when(scorer.isAgentRelated(9)).thenReturn(true);
		when(classifier.classify(readme.getContent())).thenReturn("framework");
		when(summaryClient.summarize(repository, readme, fileTree))
			.thenReturn(new AgentTraceSummaryClient.RepositorySummaryResult(true, "AgentTrace summary", null));

		service.scoreRepositories(List.of(repository), new SyncStatistics());

		assertThat(repository.getReadmeSummary()).isEqualTo("AgentTrace summary");
		assertThat(repository.isAgentRelated()).isTrue();
		assertThat(repository.getAgentCategory()).isEqualTo("framework");
	}

	@Test
	void scoreRepositoriesKeepsExistingSummaryWhenAgentTraceFails() {
		AgentRepository repository = AgentRepository.create(repositoryDto(6L, "acme/existing-summary-agent"));
		repository.updateScoring(7, true, "framework", "Existing summary");
		var readme = com.yongoh.agenthub_backend.repository.model.RepositoryReadme.create(
			repository,
			"README.md",
			"readme-sha",
			"# Existing Summary Agent",
			24,
			false
		);
		RepositoryFileTree fileTree = RepositoryFileTree.create(repository, "[{\"path\":\"README.md\",\"type\":\"file\"}]", 1);
		when(readmeJpaRepository.findByRepository(repository)).thenReturn(Optional.of(readme));
		when(fileTreeJpaRepository.findByRepository(repository)).thenReturn(Optional.of(fileTree));
		when(scorer.score(repository, readme.getContent())).thenReturn(9);
		when(scorer.isAgentRelated(9)).thenReturn(true);
		when(classifier.classify(readme.getContent())).thenReturn("framework");
		when(summaryClient.summarize(repository, readme, fileTree))
			.thenReturn(new AgentTraceSummaryClient.RepositorySummaryResult(false, null, "agenttrace failed"));

		service.scoreRepositories(List.of(repository), new SyncStatistics());

		assertThat(repository.getReadmeSummary()).isEqualTo("Existing summary");
	}

	private GithubRepositoryDto repositoryDto(Long id, String fullName) {
		String[] parts = fullName.split("/");
		return new GithubRepositoryDto(
			id,
			fullName,
			parts[0],
			parts[1],
			"Agent repository",
			"https://github.com/" + fullName,
			"https://github.com/" + fullName + ".git",
			null,
			"main",
			"Java",
			List.of("ai-agent"),
			100,
			5,
			8,
			1,
			"MIT",
			Instant.parse("2026-01-01T00:00:00Z"),
			Instant.parse("2025-01-01T00:00:00Z"),
			Instant.parse("2026-01-01T00:00:00Z"),
			false,
			false
		);
	}
}
