package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.yongoh.agenthub_backend.repository.dto.RepositoryAnalysisResponse;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;
import com.yongoh.agenthub_backend.repository.repository.AgentTraceAnalysisResultRepository;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryReadmeJpaRepository;

class RepositoryQueryServiceTest {
	private final AgentRepositoryJpaRepository repositoryJpaRepository = mock(AgentRepositoryJpaRepository.class);
	private final RepositoryReadmeJpaRepository readmeJpaRepository = mock(RepositoryReadmeJpaRepository.class);
	private final RepositoryAnalysisRepository analysisRepository = mock(RepositoryAnalysisRepository.class);
	private final AgentTraceAnalysisResultRepository agentTraceAnalysisResultRepository = mock(AgentTraceAnalysisResultRepository.class);
	private final AnalysisService analysisService = mock(AnalysisService.class);
	private final RepositoryQueryService service = new RepositoryQueryService(
		repositoryJpaRepository,
		readmeJpaRepository,
		analysisRepository,
		agentTraceAnalysisResultRepository,
		analysisService
	);

	@Test
	void requestAnalysisCreatesNewRunWhenLatestAnalysisIsCompleted() {
		UUID repositoryId = UUID.randomUUID();
		AgentRepository repository = mockRepository(repositoryId, "https://github.com/acme/agent");
		RepositoryAnalysis completed = analysis(repositoryId, "COMPLETED");
		RepositoryAnalysis processing = analysis(repositoryId, "PROCESSING");
		when(repositoryJpaRepository.findById(repositoryId)).thenReturn(Optional.of(repository));
		when(readmeJpaRepository.findByRepository(repository)).thenReturn(Optional.of(mock(RepositoryReadme.class)));
		when(analysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId)).thenReturn(Optional.of(completed));
		when(agentTraceAnalysisResultRepository.findByAnalysisId(completed.getAnalysisId())).thenReturn(Optional.empty());
		when(analysisService.requestAnalysis(any(), any(), any(), any())).thenReturn(processing);

		var response = service.requestAnalysis(repositoryId);

		assertThat(response.getStatus()).isEqualTo("PROCESSING");
		verify(analysisService).requestAnalysis(eq(repositoryId), any(UUID.class), isNull(), eq("https://github.com/acme/agent"));
	}

	@Test
	void requestAnalysisReusesActiveAnalysis() {
		UUID repositoryId = UUID.randomUUID();
		AgentRepository repository = mockRepository(repositoryId, "https://github.com/acme/agent");
		RepositoryAnalysis running = analysis(repositoryId, "RUNNING");
		when(repositoryJpaRepository.findById(repositoryId)).thenReturn(Optional.of(repository));
		when(readmeJpaRepository.findByRepository(repository)).thenReturn(Optional.of(mock(RepositoryReadme.class)));
		when(analysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId)).thenReturn(Optional.of(running));
		when(agentTraceAnalysisResultRepository.findByAnalysisId(running.getAnalysisId())).thenReturn(Optional.empty());

		var response = service.requestAnalysis(repositoryId);

		assertThat(response.getStatus()).isEqualTo("RUNNING");
		verify(analysisService, never()).requestAnalysis(any(), any(), any(), any());
	}

	@Test
	void requestAnalysisReturnsAgentTraceResultWhenActiveBackendRowAlreadyCompletedThere() {
		UUID repositoryId = UUID.randomUUID();
		AgentRepository repository = mockRepository(repositoryId, "https://github.com/acme/agent");
		RepositoryAnalysis queued = analysis(repositoryId, "PENDING");
		RepositoryAnalysisResponse completed = new RepositoryAnalysisResponse(
			queued.getAnalysisId(),
			repositoryId,
			queued.getSnapshotId(),
			"completed",
			"{\"summary\":\"done\"}",
			null,
			null,
			null,
			null,
			null
		);
		when(repositoryJpaRepository.findById(repositoryId)).thenReturn(Optional.of(repository));
		when(readmeJpaRepository.findByRepository(repository)).thenReturn(Optional.of(mock(RepositoryReadme.class)));
		when(analysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId)).thenReturn(Optional.of(queued));
		when(agentTraceAnalysisResultRepository.findByAnalysisId(queued.getAnalysisId())).thenReturn(Optional.of(completed));

		var response = service.requestAnalysis(repositoryId);

		assertThat(response.getStatus()).isEqualTo("completed");
		assertThat(response.getResultJson()).contains("done");
		verify(analysisService, never()).requestAnalysis(any(), any(), any(), any());
	}

	private AgentRepository mockRepository(UUID repositoryId, String htmlUrl) {
		AgentRepository repository = mock(AgentRepository.class);
		when(repository.getId()).thenReturn(repositoryId);
		when(repository.getHtmlUrl()).thenReturn(htmlUrl);
		return repository;
	}

	private RepositoryAnalysis analysis(UUID repositoryId, String status) {
		return RepositoryAnalysis.builder()
			.analysisId(UUID.randomUUID())
			.repositoryId(repositoryId)
			.snapshotId(UUID.randomUUID())
			.status(status)
			.createdAt(Instant.now())
			.updatedAt(Instant.now())
			.build();
	}
}
