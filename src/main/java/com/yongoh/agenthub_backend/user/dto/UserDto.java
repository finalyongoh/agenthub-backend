package com.yongoh.agenthub_backend.user.dto;

import java.util.UUID;

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

	public static UserDto from(User user) {
		return new UserDto(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			user.getProfileImageFilename() == null ? null : "/api/images/" + user.getProfileImageFilename(),
			user.getRole().name()
		);
	}

}
