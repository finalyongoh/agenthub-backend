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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "repository_notifications",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_repository_notifications_unique_change",
		columnNames = {"user_id", "repository_id", "type", "change_key"}
	),
	indexes = {
		@Index(name = "idx_repository_notifications_user_read", columnList = "user_id, is_read"),
		@Index(name = "idx_repository_notifications_repository_id", columnList = "repository_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryNotification {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private AgentRepository repository;

	@Column(nullable = false, length = 50)
	private String type;

	@Column(name = "change_key", nullable = false, length = 200)
	private String changeKey;

	@Column(nullable = false, length = 500)
	private String message;

	@Column(name = "old_sha", length = 100)
	private String oldSha;

	@Column(name = "new_sha", length = 100)
	private String newSha;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	@Column(name = "read_at")
	private Instant readAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static RepositoryNotification changed(
		User user,
		AgentRepository repository,
		String type,
		String fieldName,
		String oldSha,
		String newSha
	) {
		RepositoryNotification notification = new RepositoryNotification();
		notification.id = UUID.randomUUID();
		notification.user = user;
		notification.repository = repository;
		notification.type = type;
		notification.changeKey = changeKey(fieldName, newSha);
		notification.message = repository.getFullName() + " " + fieldName + " 변경사항이 감지되었습니다.";
		notification.oldSha = oldSha;
		notification.newSha = newSha;
		notification.read = false;
		return notification;
	}

	public static String changeKey(String fieldName, String value) {
		String raw = fieldName + ":" + (value == null ? "" : value);
		if (raw.length() <= 200) {
			return raw;
		}
		return fieldName + ":" + Integer.toHexString(raw.hashCode());
	}

	public void markAsRead() {
		if (!read) {
			this.read = true;
			this.readAt = Instant.now();
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
