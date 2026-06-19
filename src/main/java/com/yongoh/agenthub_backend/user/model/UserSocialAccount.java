package com.yongoh.agenthub_backend.user.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
	name = "user_social_accounts",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_social_provider_provider_id", columnNames = {"provider", "provider_id"}),
		@UniqueConstraint(name = "uk_social_user_provider", columnNames = {"user_id", "provider"})
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSocialAccount {
	@Id
	@Column(name = "social_account_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SocialProvider provider;

	@Column(name = "provider_id", nullable = false, length = 255)
	private String providerId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static UserSocialAccount create(User user, SocialProvider provider, String providerId) {
		UserSocialAccount account = new UserSocialAccount();
		account.id = UUID.randomUUID();
		account.user = user;
		account.provider = provider;
		account.providerId = providerId;
		return account;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
