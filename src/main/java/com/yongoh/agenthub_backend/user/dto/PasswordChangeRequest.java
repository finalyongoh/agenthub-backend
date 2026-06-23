package com.yongoh.agenthub_backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
	private String currentPassword;
	private String newPassword;
}
