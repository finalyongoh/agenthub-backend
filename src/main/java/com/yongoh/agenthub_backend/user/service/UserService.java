package com.yongoh.agenthub_backend.user.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.yongoh.agenthub_backend.community.service.PostImageStorageService;
import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.user.dto.JwtResponse;
import com.yongoh.agenthub_backend.user.dto.LoginRequest;
import com.yongoh.agenthub_backend.user.dto.PasswordChangeRequest;
import com.yongoh.agenthub_backend.user.dto.SignupRequest;
import com.yongoh.agenthub_backend.user.dto.UserDto;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.repository.UserRepository;
import com.yongoh.agenthub_backend.user.util.JwtUtil;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final PostImageStorageService imageStorageService;

	public UserService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtUtil jwtUtil,
		PostImageStorageService imageStorageService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.imageStorageService = imageStorageService;
	}

	@Transactional
	public JwtResponse signup(SignupRequest request) {
		validateSignupRequest(request);
		String email = normalizeEmail(request.getEmail());
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "AUTH_409", "이미 가입된 이메일입니다.");
		}

		// 비밀번호는 평문으로 저장하지 않고 BCrypt 해시만 저장한다.
		User user = User.create(email, passwordEncoder.encode(request.getPassword()), request.getNickname().trim());
		User savedUser = userRepository.save(user);
		return issueToken(savedUser);
	}

	@Transactional(readOnly = true)
	public JwtResponse login(LoginRequest request) {
		validateLoginRequest(request);
		User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "이메일 또는 비밀번호가 올바르지 않습니다."));

		if (user.isRestricted() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "이메일 또는 비밀번호가 올바르지 않습니다.");
		}
		return issueToken(user);
	}

	@Transactional(readOnly = true)
	public UserDto me(UUID userId) {
		return UserDto.from(findActiveUser(userId));
	}

	@Transactional
	public UserDto updateProfileImage(UUID userId, MultipartFile image) {
		User user = findActiveUser(userId);
		String filename = imageStorageService.store(image);
		if (filename == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE_REQUIRED", "프로필 이미지를 선택해주세요.");
		}
		user.updateProfileImage(filename);
		return UserDto.from(user);
	}

	@Transactional
	public UserDto changePassword(UUID userId, PasswordChangeRequest request) {
		User user = findActiveUser(userId);
		validatePasswordChangeRequest(request);
		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_400", "현재 비밀번호가 올바르지 않습니다.");
		}
		user.changePassword(passwordEncoder.encode(request.getNewPassword()));
		return UserDto.from(user);
	}

	private JwtResponse issueToken(User user) {
		return new JwtResponse(jwtUtil.createAccessToken(user), UserDto.from(user));
	}

	private User findActiveUser(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."));
		if (user.isRestricted()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_403", "제한된 사용자입니다.");
		}
		return user;
	}

	private void validateSignupRequest(SignupRequest request) {
		if (request == null
			|| !StringUtils.hasText(request.getEmail())
			|| !StringUtils.hasText(request.getPassword())
			|| !StringUtils.hasText(request.getNickname())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "COMMON_400", "필수 입력값이 누락되었습니다.");
		}
		if (request.getPassword().length() < 8) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_400", "비밀번호는 8자 이상이어야 합니다.");
		}
	}

	private void validateLoginRequest(LoginRequest request) {
		if (request == null || !StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getPassword())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "COMMON_400", "필수 입력값이 누락되었습니다.");
		}
	}

	private void validatePasswordChangeRequest(PasswordChangeRequest request) {
		if (request == null || !StringUtils.hasText(request.getCurrentPassword()) || !StringUtils.hasText(request.getNewPassword())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "COMMON_400", "필수 입력값이 누락되었습니다.");
		}
		if (request.getNewPassword().length() < 8) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_400", "비밀번호는 8자 이상이어야 합니다.");
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
