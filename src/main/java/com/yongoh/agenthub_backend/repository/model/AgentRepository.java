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
	private static final int MAX_AGENT_SCORE = 90;

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

	@Column(name = "description_ko", columnDefinition = "text")
	private String descriptionKo;

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
		this.githubId = dto.getGithubId();
		this.fullName = dto.getFullName();
		this.owner = dto.getOwner();
		this.name = dto.getName();
		this.description = dto.getDescription();
		this.htmlUrl = dto.getHtmlUrl();
		this.cloneUrl = dto.getCloneUrl();
		this.homepage = dto.getHomepage();
		this.defaultBranch = dto.getDefaultBranch();
		this.language = dto.getLanguage();
		this.topics = String.join(",", dto.getTopics());
		this.stars = dto.getStars();
		this.forks = dto.getForks();
		this.watchers = dto.getWatchers();
		this.openIssues = dto.getOpenIssues();
		this.license = dto.getLicense();
		this.pushedAt = dto.getPushedAt();
		this.githubCreatedAt = dto.getGithubCreatedAt();
		this.githubUpdatedAt = dto.getGithubUpdatedAt();
		this.archived = dto.isArchived();
		this.fork = dto.isFork();
	}

	public void updateDescriptionKo(String descriptionKo) {
		this.descriptionKo = descriptionKo;
	}

	public void markReadmeFetched() {
		this.lastFetchedAt = Instant.now();
	}

	public void updateScoring(int agentScore, boolean agentRelated, String agentCategory, String readmeSummary) {
		this.agentScore = normalizeAgentScore(agentScore);
		this.agentRelated = agentRelated;
		this.agentCategory = agentCategory;
		this.readmeSummary = readmeSummary;
	}

	public int getAgentScore() {
		return normalizeAgentScore(agentScore);
	}

	private int normalizeAgentScore(int value) {
		return Math.max(0, Math.min(MAX_AGENT_SCORE, value));
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
