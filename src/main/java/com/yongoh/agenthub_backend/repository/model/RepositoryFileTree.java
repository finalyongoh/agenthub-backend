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
@Table(name = "repository_file_trees", indexes = @Index(name = "idx_repository_file_trees_fetched_at", columnList = "fetched_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryFileTree {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false, unique = true)
	private AgentRepository repository;

	@Column(name = "tree_json", columnDefinition = "text", nullable = false)
	private String treeJson;

	@Column(name = "path_count", nullable = false)
	private int pathCount;

	@Column(name = "fetched_at", nullable = false)
	private Instant fetchedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static RepositoryFileTree create(AgentRepository repository, String treeJson, int pathCount) {
		RepositoryFileTree fileTree = new RepositoryFileTree();
		fileTree.id = UUID.randomUUID();
		fileTree.repository = repository;
		fileTree.update(treeJson, pathCount);
		return fileTree;
	}

	public void update(String treeJson, int pathCount) {
		this.treeJson = treeJson;
		this.pathCount = pathCount;
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
