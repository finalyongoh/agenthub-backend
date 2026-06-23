package com.yongoh.agenthub_backend.user.dto;

import java.util.UUID;

import com.yongoh.agenthub_backend.user.model.SocialProvider;
import com.yongoh.agenthub_backend.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
	private UUID id;
	private String email;
	private String nickname;
	private String profileImageUrl;
	private String role;
	private String loginProvider;

	public static UserDto from(User user) {
		return from(user, "EMAIL");
	}

	public static UserDto from(User user, SocialProvider loginProvider) {
		return from(user, loginProvider == null ? "EMAIL" : loginProvider.name());
	}

	public static UserDto from(User user, String loginProvider) {
		return new UserDto(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			user.getProfileImageFilename() == null ? null : "/api/images/" + user.getProfileImageFilename(),
			user.getRole().name(),
			loginProvider
		);
	}

}
