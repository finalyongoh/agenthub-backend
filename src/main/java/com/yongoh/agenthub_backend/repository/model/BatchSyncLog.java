package com.yongoh.agenthub_backend.repository.model;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.service.SyncStatistics;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "batch_sync_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchSyncLog {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "job_name", nullable = false, length = 100)
	private String jobName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BatchSyncLogStatus status;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "ended_at")
	private Instant endedAt;

	@Column(name = "searched_count", nullable = false)
	private int searchedCount;

	@Column(name = "saved_count", nullable = false)
	private int savedCount;

	@Column(name = "readme_fetched_count", nullable = false)
	private int readmeFetchedCount;

	@Column(name = "agent_related_count", nullable = false)
	private int agentRelatedCount;

	@Column(name = "skipped_count", nullable = false)
	private int skippedCount;

	@Column(name = "failed_count", nullable = false)
	private int failedCount;

	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static BatchSyncLog started(String jobName) {
		BatchSyncLog log = new BatchSyncLog();
		log.id = UUID.randomUUID();
		log.jobName = jobName;
		log.status = BatchSyncLogStatus.STARTED;
		log.startedAt = Instant.now();
		return log;
	}

	public void complete(SyncStatistics statistics) {
		this.status = BatchSyncLogStatus.COMPLETED;
		this.endedAt = Instant.now();
		apply(statistics);
	}

	public void fail(SyncStatistics statistics, String errorMessage) {
		this.status = BatchSyncLogStatus.FAILED;
		this.endedAt = Instant.now();
		this.errorMessage = errorMessage;
		apply(statistics);
	}

	private void apply(SyncStatistics statistics) {
		this.searchedCount = statistics.getSearchedCount();
		this.savedCount = statistics.getSavedCount();
		this.readmeFetchedCount = statistics.getReadmeFetchedCount();
		this.agentRelatedCount = statistics.getAgentRelatedCount();
		this.skippedCount = statistics.getSkippedCount();
		this.failedCount = statistics.getFailedCount();
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
