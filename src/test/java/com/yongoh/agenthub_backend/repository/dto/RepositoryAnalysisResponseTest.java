package com.yongoh.agenthub_backend.repository.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;

class RepositoryAnalysisResponseTest {
	@Test
	void fromIncludesResultAndErrorFields() {
		UUID analysisId = UUID.randomUUID();
		UUID repositoryId = UUID.randomUUID();
		UUID snapshotId = UUID.randomUUID();
		Instant createdAt = Instant.parse("2026-06-24T10:00:00Z");
		Instant updatedAt = Instant.parse("2026-06-24T10:03:00Z");
		RepositoryAnalysis analysis = RepositoryAnalysis.builder()
			.analysisId(analysisId)
			.repositoryId(repositoryId)
			.snapshotId(snapshotId)
			.status("FAILED")
			.resultJson("{\"summary\":\"partial\"}")
			.errorMessage("AgentTrace timeout")
			.createdAt(createdAt)
			.updatedAt(updatedAt)
			.build();

		RepositoryAnalysisResponse response = RepositoryAnalysisResponse.from(analysis);

		assertThat(response.getAnalysisId()).isEqualTo(analysisId);
		assertThat(response.getRepositoryId()).isEqualTo(repositoryId);
		assertThat(response.getSnapshotId()).isEqualTo(snapshotId);
		assertThat(response.getStatus()).isEqualTo("FAILED");
		assertThat(response.getResultJson()).isEqualTo("{\"summary\":\"partial\"}");
		assertThat(response.getErrorMessage()).isEqualTo("AgentTrace timeout");
		assertThat(response.getCreatedAt()).isEqualTo(createdAt);
		assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
	}
}
