package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.BatchSyncLog;

public record BatchSyncLogDto(
	UUID id,
	String jobName,
	String status,
	Instant startedAt,
	Instant endedAt,
	int searchedCount,
	int savedCount,
	int readmeFetchedCount,
	int agentRelatedCount,
	int skippedCount,
	int failedCount,
	String errorMessage
) {
	public static BatchSyncLogDto from(BatchSyncLog log) {
		return new BatchSyncLogDto(
			log.getId(),
			log.getJobName(),
			log.getStatus().name().toLowerCase(),
			log.getStartedAt(),
			log.getEndedAt(),
			log.getSearchedCount(),
			log.getSavedCount(),
			log.getReadmeFetchedCount(),
			log.getAgentRelatedCount(),
			log.getSkippedCount(),
			log.getFailedCount(),
			log.getErrorMessage()
		);
	}
}
