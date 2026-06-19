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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "discussion_comments",
	indexes = {
		@Index(name = "idx_discussion_comments_discussion_created", columnList = "discussion_id, created_at"),
		@Index(name = "idx_discussion_comments_user_id", columnList = "user_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscussionComment {
	@Id
	@Column(name = "comment_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "discussion_id", nullable = false)
	private RepositoryDiscussion discussion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, columnDefinition = "text")
	private String body;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static DiscussionComment create(RepositoryDiscussion discussion, User user, String body) {
		DiscussionComment comment = new DiscussionComment();
		comment.id = UUID.randomUUID();
		comment.discussion = discussion;
		comment.user = user;
		comment.body = body;
		return comment;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
