package com.yongoh.agenthub_backend.repository.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "repository_analyses",
	indexes = {
		@Index(name = "idx_repository_analyses_repository_id", columnList = "repository_id"),
		@Index(name = "idx_repository_analyses_status", columnList = "status")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryAnalysis {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private AgentRepository repository;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RepositoryAnalysisStatus status;

	@Column(columnDefinition = "text")
	private String summary;

	@Column(columnDefinition = "text")
	private String features;

	@Column(name = "tech_stack", columnDefinition = "text")
	private String techStack;

	@Column(columnDefinition = "text")
	private String architecture;

	@Column(name = "use_cases", columnDefinition = "text")
	private String useCases;

	@Column(columnDefinition = "text")
	private String strengths;

	@Column(columnDefinition = "text")
	private String limitations;

	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static RepositoryAnalysis pending(AgentRepository repository) {
		RepositoryAnalysis analysis = new RepositoryAnalysis();
		analysis.id = UUID.randomUUID();
		analysis.repository = repository;
		analysis.status = RepositoryAnalysisStatus.PENDING;
		analysis.requestedAt = Instant.now();
		return analysis;
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
