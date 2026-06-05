package com.yongoh.agenthub_backend.repository.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "repository_readmes", indexes = @Index(name = "idx_repository_readmes_sha", columnList = "sha"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryReadme {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false, unique = true)
	private AgentRepository repository;

	@Column(nullable = false, length = 200)
	private String path;

	@Column(length = 100)
	private String sha;

	@Column(columnDefinition = "text")
	private String content;

	@Column(name = "content_length", nullable = false)
	private int contentLength;

	@Column(name = "is_truncated", nullable = false)
	private boolean truncated;

	@Column(name = "fetched_at", nullable = false)
	private Instant fetchedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static RepositoryReadme create(AgentRepository repository, String path, String sha, String content, int contentLength, boolean truncated) {
		RepositoryReadme readme = new RepositoryReadme();
		readme.id = UUID.randomUUID();
		readme.repository = repository;
		readme.update(path, sha, content, contentLength, truncated);
		return readme;
	}

	public boolean hasSameSha(String sha) {
		return this.sha != null && this.sha.equals(sha);
	}

	public void update(String path, String sha, String content, int contentLength, boolean truncated) {
		this.path = path;
		this.sha = sha;
		this.content = content;
		this.contentLength = contentLength;
		this.truncated = truncated;
		this.fetchedAt = Instant.now();
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
