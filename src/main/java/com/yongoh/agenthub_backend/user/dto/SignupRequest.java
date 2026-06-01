package com.yongoh.agenthub_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignupRequest {
	private String email;
	private String password;
	private String nickname;
}
