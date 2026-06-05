package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.BatchSyncLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BatchSyncLogDto {
	private UUID id;
	private String jobName;
	private String status;
	private Instant startedAt;
	private Instant endedAt;
	private int searchedCount;
	private int savedCount;
	private int readmeFetchedCount;
	private int agentRelatedCount;
	private int skippedCount;
	private int failedCount;
	private String errorMessage;

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
