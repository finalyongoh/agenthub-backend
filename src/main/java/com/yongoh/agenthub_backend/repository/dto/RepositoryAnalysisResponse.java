package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryAnalysisResponse {
	private UUID analysisId;
	private UUID repositoryId;
	private UUID snapshotId;
	private String status;
	private String resultJson;
	private String errorMessage;
	private Instant createdAt;
	private Instant updatedAt;

	public static RepositoryAnalysisResponse from(RepositoryAnalysis analysis) {
		return new RepositoryAnalysisResponse(
			analysis.getAnalysisId(),
			analysis.getRepositoryId(),
			analysis.getSnapshotId(),
			analysis.getStatus(),
			analysis.getResultJson(),
			analysis.getErrorMessage(),
			analysis.getCreatedAt(),
			analysis.getUpdatedAt()
		);
	}
}
