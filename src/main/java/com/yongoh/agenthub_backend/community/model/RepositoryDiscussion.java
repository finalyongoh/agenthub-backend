package com.yongoh.agenthub_backend.community.model;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.user.model.User;
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
	name = "repository_discussions",
	indexes = {
		@Index(name = "idx_repository_discussions_repository_created", columnList = "repository_id, created_at"),
		@Index(name = "idx_repository_discussions_user_id", columnList = "user_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryDiscussion {
	@Id
	@Column(name = "discussion_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "repository_id", nullable = false)
	private UUID repositoryId;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String body;

	@Column(name = "image_filename", length = 100)
	private String imageFilename;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PostStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static RepositoryDiscussion create(User user, UUID repositoryId, String title, String body) {
		return create(user, repositoryId, title, body, null);
	}

	public static RepositoryDiscussion create(User user, UUID repositoryId, String title, String body, String imageFilename) {
		RepositoryDiscussion discussion = new RepositoryDiscussion();
		discussion.id = UUID.randomUUID();
		discussion.user = user;
		discussion.repositoryId = repositoryId;
		discussion.title = title;
		discussion.body = body;
		discussion.imageFilename = imageFilename;
		discussion.status = PostStatus.ACTIVE;
		return discussion;
	}

	public void hide() {
		this.status = PostStatus.HIDDEN;
	}

	public void delete() {
		this.status = PostStatus.DELETED;
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
