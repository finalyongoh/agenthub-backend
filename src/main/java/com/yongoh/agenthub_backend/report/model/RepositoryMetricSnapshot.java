package com.yongoh.agenthub_backend.report.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "repository_metric_snapshots",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_repository_metric_snapshot_day",
		columnNames = {"repository_id", "captured_on"}
	),
	indexes = @Index(name = "idx_metric_snapshots_day", columnList = "captured_on")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryMetricSnapshot {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private AgentRepository repository;

	@Column(name = "captured_on", nullable = false)
	private LocalDate capturedOn;

	@Column(nullable = false)
	private int stars;

	@Column(nullable = false)
	private int forks;

	@Column(nullable = false)
	private int watchers;

	@Column(name = "open_issues", nullable = false)
	private int openIssues;

	@Column(name = "pushed_at")
	private Instant pushedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static RepositoryMetricSnapshot capture(AgentRepository repository, LocalDate capturedOn) {
		RepositoryMetricSnapshot snapshot = new RepositoryMetricSnapshot();
		snapshot.id = UUID.randomUUID();
		snapshot.repository = repository;
		snapshot.capturedOn = capturedOn;
		snapshot.stars = repository.getStars();
		snapshot.forks = repository.getForks();
		snapshot.watchers = repository.getWatchers();
		snapshot.openIssues = repository.getOpenIssues();
		snapshot.pushedAt = repository.getPushedAt();
		snapshot.createdAt = Instant.now();
		return snapshot;
	}
}
