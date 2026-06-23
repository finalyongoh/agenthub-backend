package com.yongoh.agenthub_backend.community.model;

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
	name = "post_likes",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_post_likes_post_user", columnNames = {"post_id", "user_id"})
	},
	indexes = {
		@Index(name = "idx_post_likes_post_id", columnList = "post_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {
	@Id
	@Column(name = "like_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static PostLike create(Post post, User user) {
		PostLike like = new PostLike();
		like.id = UUID.randomUUID();
		like.post = post;
		like.user = user;
		return like;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
