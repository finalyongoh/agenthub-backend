package com.yongoh.agenthub_backend.repository.dto;

import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;

public record RepositoryAnalysisResponse(UUID analysisId, UUID repositoryId, String status) {
	public static RepositoryAnalysisResponse from(RepositoryAnalysis analysis) {
		return new RepositoryAnalysisResponse(
			analysis.getId(),
			analysis.getRepository().getId(),
			analysis.getStatus().name().toLowerCase()
		);
	}
}
