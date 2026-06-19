package com.yongoh.agenthub_backend.global.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.user.dto.JwtResponse;
import com.yongoh.agenthub_backend.user.model.SocialProvider;
import com.yongoh.agenthub_backend.user.service.OAuthUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
	private final OAuthUserService oauthUserService;
	private final String frontendBaseUrl;

	public OAuth2LoginSuccessHandler(
		OAuthUserService oauthUserService,
		@Value("${agenthub.frontend.base-url}") String frontendBaseUrl
	) {
		this.oauthUserService = oauthUserService;
		this.frontendBaseUrl = frontendBaseUrl;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
		throws IOException {
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		SocialProvider provider = SocialProvider.valueOf(oauthToken.getAuthorizedClientRegistrationId().toUpperCase());
		OAuth2User oauthUser = oauthToken.getPrincipal();
		Map<String, Object> attributes = oauthUser.getAttributes();

		String providerId = resolveProviderId(provider, attributes);
		String email = resolveEmail(attributes);
		String nickname = resolveNickname(provider, attributes, email);
		if (!StringUtils.hasText(providerId) || !StringUtils.hasText(email)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "소셜 계정 정보를 가져오지 못했습니다.");
		}

		JwtResponse jwtResponse = oauthUserService.loginOrSignup(provider, providerId, email, nickname);
		response.sendRedirect(frontendBaseUrl + "/oauth/callback#accessToken=" + encode(jwtResponse.getAccessToken()));
	}

	private String resolveProviderId(SocialProvider provider, Map<String, Object> attributes) {
		Object value = provider == SocialProvider.GOOGLE ? attributes.get("sub") : attributes.get("id");
		return value == null ? null : String.valueOf(value);
	}

	private String resolveEmail(Map<String, Object> attributes) {
		Object value = attributes.get("email");
		return value == null ? null : String.valueOf(value);
	}

	private String resolveNickname(SocialProvider provider, Map<String, Object> attributes, String email) {
		Object name = attributes.get("name");
		if (name instanceof String text && StringUtils.hasText(text)) {
			return text;
		}
		Object login = provider == SocialProvider.GITHUB ? attributes.get("login") : null;
		if (login != null && StringUtils.hasText(String.valueOf(login))) {
			return String.valueOf(login);
		}
		return email.split("@")[0];
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
