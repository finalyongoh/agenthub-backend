package com.yongoh.agenthub_backend.user.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_email", columnNames = "email")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
	@Id
	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(name = "password", nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 100)
	private String nickname;

	@Column(name = "profile_image_filename", length = 255)
	private String profileImageFilename;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static User create(String email, String encodedPassword, String nickname) {
		User user = new User();
		user.id = UUID.randomUUID();
		user.email = email;
		user.password = encodedPassword;
		user.nickname = nickname;
		// 최초 회원가입 사용자는 일반 사용자 권한으로 시작한다.
		user.role = UserRole.USER;
		user.status = UserStatus.ACTIVE;
		return user;
	}

	public boolean isRestricted() {
		return status != UserStatus.ACTIVE;
	}

	public void updateProfileImage(String profileImageFilename) {
		this.profileImageFilename = profileImageFilename;
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
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
