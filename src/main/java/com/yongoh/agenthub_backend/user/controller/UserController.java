package com.yongoh.agenthub_backend.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yongoh.agenthub_backend.global.security.AuthenticatedUser;
import com.yongoh.agenthub_backend.user.dto.JwtResponse;
import com.yongoh.agenthub_backend.user.dto.LoginRequest;
import com.yongoh.agenthub_backend.user.dto.PasswordChangeRequest;
import com.yongoh.agenthub_backend.user.dto.SignupRequest;
import com.yongoh.agenthub_backend.user.dto.UserDto;
import com.yongoh.agenthub_backend.user.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/signup")
	public JwtResponse signup(@RequestBody SignupRequest request) {
		// 회원가입 성공 시 바로 사용할 수 있는 JWT를 함께 발급한다.
		return userService.signup(request);
	}

	@PostMapping("/login")
	public JwtResponse login(@RequestBody LoginRequest request) {
		return userService.login(request);
	}

	@PostMapping("/logout")
	public void logout(@AuthenticationPrincipal AuthenticatedUser user) {
		// 현재 구현은 stateless JWT라 서버에서 삭제할 세션이 없다.
	}

	@GetMapping("/me")
	public UserDto me(@AuthenticationPrincipal AuthenticatedUser user) {
		return userService.me(user.getId(), user.getLoginProvider());
	}

	@PostMapping(value = "/me/profile-image", consumes = "multipart/form-data")
	public UserDto updateProfileImage(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestPart("image") MultipartFile image
	) {
		return userService.updateProfileImage(user.getId(), image, user.getLoginProvider());
	}

	@PatchMapping("/me/password")
	public UserDto changePassword(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestBody PasswordChangeRequest request
	) {
		return userService.changePassword(user.getId(), request, user.getLoginProvider());
	}
}
