package com.yongoh.agenthub_backend.user.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.yongoh.agenthub_backend.user.dto.JwtResponse;
import com.yongoh.agenthub_backend.user.dto.UserDto;
import com.yongoh.agenthub_backend.user.model.SocialProvider;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.model.UserSocialAccount;
import com.yongoh.agenthub_backend.user.repository.UserRepository;
import com.yongoh.agenthub_backend.user.repository.UserSocialAccountRepository;
import com.yongoh.agenthub_backend.user.util.JwtUtil;

@Service
public class OAuthUserService {
	private final UserRepository userRepository;
	private final UserSocialAccountRepository socialAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public OAuthUserService(
		UserRepository userRepository,
		UserSocialAccountRepository socialAccountRepository,
		PasswordEncoder passwordEncoder,
		JwtUtil jwtUtil
	) {
		this.userRepository = userRepository;
		this.socialAccountRepository = socialAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	@Transactional
	public JwtResponse loginOrSignup(SocialProvider provider, String providerId, String email, String nickname) {
		User user = socialAccountRepository.findByProviderAndProviderId(provider, providerId)
			.map(UserSocialAccount::getUser)
			.orElseGet(() -> createOrLinkUser(provider, providerId, normalizeEmail(email), resolveNickname(nickname, email)));

		return new JwtResponse(jwtUtil.createAccessToken(user), UserDto.from(user, provider));
	}

	private User createOrLinkUser(SocialProvider provider, String providerId, String email, String nickname) {
		User user = userRepository.findByEmail(email)
			.orElseGet(() -> userRepository.save(User.create(email, passwordEncoder.encode(UUID.randomUUID().toString()), nickname)));
		socialAccountRepository.save(UserSocialAccount.create(user, provider, providerId));
		return user;
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String resolveNickname(String nickname, String email) {
		if (StringUtils.hasText(nickname)) {
			return nickname.trim();
		}
		return email.split("@")[0];
	}
}
