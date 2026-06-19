package com.yongoh.agenthub_backend.repository.dto;

import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryAnalysisResponse {
	private UUID analysisId;
	private UUID repositoryId;
	private String status;

	public static RepositoryAnalysisResponse from(RepositoryAnalysis analysis) {
		return new RepositoryAnalysisResponse(
			analysis.getAnalysisId(),
			analysis.getRepositoryId(),
			analysis.getStatus()
		);
	}
}
