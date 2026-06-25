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
	name = "posts",
	indexes = {
		@Index(name = "idx_posts_created_at", columnList = "created_at"),
		@Index(name = "idx_posts_user_id", columnList = "user_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
	@Id
	@Column(name = "post_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

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

	public static Post create(User user, String title, String body) {
		return create(user, title, body, null);
	}

	public static Post create(User user, String title, String body, String imageFilename) {
		Post post = new Post();
		post.id = UUID.randomUUID();
		post.user = user;
		post.title = title;
		post.body = body;
		post.imageFilename = imageFilename;
		post.status = PostStatus.ACTIVE;
		return post;
	}

	public void hide() {
		this.status = PostStatus.HIDDEN;
	}

	public void delete() {
		this.status = PostStatus.DELETED;
	}

	public void update(String title, String body, String imageFilename) {
		this.title = title;
		this.body = body;
		if (imageFilename != null) {
			this.imageFilename = imageFilename;
		}
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
