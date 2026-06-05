package com.yongoh.agenthub_backend.repository.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "repository_change_logs",
	indexes = {
		@Index(name = "idx_repository_change_logs_repository_id", columnList = "repository_id"),
		@Index(name = "idx_repository_change_logs_detected_at", columnList = "detected_at")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryChangeLog {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private AgentRepository repository;

	@Column(name = "change_type", nullable = false, length = 50)
	private String changeType;

	@Column(name = "field_name", nullable = false, length = 100)
	private String fieldName;

	@Column(name = "old_value", columnDefinition = "text")
	private String oldValue;

	@Column(name = "new_value", columnDefinition = "text")
	private String newValue;

	@Column(name = "old_sha", length = 100)
	private String oldSha;

	@Column(name = "new_sha", length = 100)
	private String newSha;

	@Column(name = "detected_at", nullable = false, updatable = false)
	private Instant detectedAt;

	public static RepositoryChangeLog create(
		AgentRepository repository,
		String changeType,
		String fieldName,
		String oldValue,
		String newValue,
		String oldSha,
		String newSha
	) {
		RepositoryChangeLog changeLog = new RepositoryChangeLog();
		changeLog.id = UUID.randomUUID();
		changeLog.repository = repository;
		changeLog.changeType = changeType;
		changeLog.fieldName = fieldName;
		changeLog.oldValue = oldValue;
		changeLog.newValue = newValue;
		changeLog.oldSha = oldSha;
		changeLog.newSha = newSha;
		return changeLog;
	}

	@PrePersist
	void prePersist() {
		this.detectedAt = Instant.now();
	}
}
