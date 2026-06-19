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
	name = "discussion_likes",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_discussion_likes_discussion_user", columnNames = {"discussion_id", "user_id"})
	},
	indexes = {
		@Index(name = "idx_discussion_likes_discussion_id", columnList = "discussion_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscussionLike {
	@Id
	@Column(name = "like_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "discussion_id", nullable = false)
	private RepositoryDiscussion discussion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static DiscussionLike create(RepositoryDiscussion discussion, User user) {
		DiscussionLike like = new DiscussionLike();
		like.id = UUID.randomUUID();
		like.discussion = discussion;
		like.user = user;
		return like;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
