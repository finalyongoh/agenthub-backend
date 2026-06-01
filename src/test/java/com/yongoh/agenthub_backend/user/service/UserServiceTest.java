package com.yongoh.agenthub_backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.user.dto.LoginRequest;
import com.yongoh.agenthub_backend.user.dto.SignupRequest;

@SpringBootTest
class UserServiceTest {
	@Autowired
	UserService userService;

	@Test
	void signupLoginAndMe() {
		String email = "user-" + System.nanoTime() + "@example.com";
		String password = "password123";

		var signupResponse = userService.signup(new SignupRequest(email, password, "agent"));

		assertThat(signupResponse.getAccessToken()).isNotBlank();
		assertThat(signupResponse.getUser().getEmail()).isEqualTo(email);

		var loginResponse = userService.login(new LoginRequest(email, password));
		assertThat(loginResponse.getAccessToken()).isNotBlank();
		assertThat(loginResponse.getUser().getNickname()).isEqualTo("agent");

		var me = userService.me(loginResponse.getUser().getId());
		assertThat(me.getEmail()).isEqualTo(email);

		assertThatThrownBy(() -> userService.login(new LoginRequest(email, "wrong-password")))
			.isInstanceOf(ApiException.class);
	}
}
