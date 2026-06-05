package com.yongoh.agenthub_backend.repository.model;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "repository_bookmarks",
	uniqueConstraints = @UniqueConstraint(name = "uk_repository_bookmarks_user_repository", columnNames = {"user_id", "repository_id"}),
	indexes = {
		@Index(name = "idx_repository_bookmarks_user_id", columnList = "user_id"),
		@Index(name = "idx_repository_bookmarks_repository_id", columnList = "repository_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryBookmark {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private AgentRepository repository;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static RepositoryBookmark create(User user, AgentRepository repository) {
		RepositoryBookmark bookmark = new RepositoryBookmark();
		bookmark.id = UUID.randomUUID();
		bookmark.user = user;
		bookmark.repository = repository;
		return bookmark;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
