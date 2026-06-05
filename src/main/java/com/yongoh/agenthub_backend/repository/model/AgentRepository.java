package com.yongoh.agenthub_backend.repository.model;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "repositories",
	indexes = {
		@Index(name = "idx_repositories_is_agent_related", columnList = "is_agent_related"),
		@Index(name = "idx_repositories_agent_category", columnList = "agent_category"),
		@Index(name = "idx_repositories_stars", columnList = "stars"),
		@Index(name = "idx_repositories_pushed_at", columnList = "pushed_at")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentRepository {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "github_id", nullable = false, unique = true)
	private Long githubId;

	@Column(name = "full_name", nullable = false, unique = true, length = 300)
	private String fullName;

	@Column(nullable = false, length = 150)
	private String owner;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "html_url", length = 500)
	private String htmlUrl;

	@Column(name = "clone_url", length = 500)
	private String cloneUrl;

	@Column(length = 500)
	private String homepage;

	@Column(name = "default_branch", length = 100)
	private String defaultBranch;

	@Column(length = 100)
	private String language;

	@Column(columnDefinition = "text")
	private String topics;

	@Column(nullable = false)
	private int stars;

	@Column(nullable = false)
	private int forks;

	@Column(nullable = false)
	private int watchers;

	@Column(name = "open_issues", nullable = false)
	private int openIssues;

	@Column(length = 200)
	private String license;

	@Column(name = "pushed_at")
	private Instant pushedAt;

	@Column(name = "github_created_at")
	private Instant githubCreatedAt;

	@Column(name = "github_updated_at")
	private Instant githubUpdatedAt;

	@Column(nullable = false)
	private boolean archived;

	@Column(nullable = false)
	private boolean fork;

	@Column(name = "agent_score", nullable = false)
	private int agentScore;

	@Column(name = "is_agent_related", nullable = false)
	private boolean agentRelated;

	@Column(name = "agent_category", length = 50)
	private String agentCategory;

	@Column(name = "readme_summary", length = 500)
	private String readmeSummary;

	@Column(name = "last_fetched_at")
	private Instant lastFetchedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static AgentRepository create(GithubRepositoryDto dto) {
		AgentRepository repository = new AgentRepository();
		repository.id = UUID.randomUUID();
		repository.updateMetadata(dto);
		return repository;
	}

	public void updateMetadata(GithubRepositoryDto dto) {
		this.githubId = dto.githubId();
		this.fullName = dto.fullName();
		this.owner = dto.owner();
		this.name = dto.name();
		this.description = dto.description();
		this.htmlUrl = dto.htmlUrl();
		this.cloneUrl = dto.cloneUrl();
		this.homepage = dto.homepage();
		this.defaultBranch = dto.defaultBranch();
		this.language = dto.language();
		this.topics = String.join(",", dto.topics());
		this.stars = dto.stars();
		this.forks = dto.forks();
		this.watchers = dto.watchers();
		this.openIssues = dto.openIssues();
		this.license = dto.license();
		this.pushedAt = dto.pushedAt();
		this.githubCreatedAt = dto.githubCreatedAt();
		this.githubUpdatedAt = dto.githubUpdatedAt();
		this.archived = dto.archived();
		this.fork = dto.fork();
	}

	public void markReadmeFetched() {
		this.lastFetchedAt = Instant.now();
	}

	public void updateScoring(int agentScore, boolean agentRelated, String agentCategory, String readmeSummary) {
		this.agentScore = agentScore;
		this.agentRelated = agentRelated;
		this.agentCategory = agentCategory;
		this.readmeSummary = readmeSummary;
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
