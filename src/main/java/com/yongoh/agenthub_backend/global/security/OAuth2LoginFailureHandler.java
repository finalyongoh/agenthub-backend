package com.yongoh.agenthub_backend.global.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
	private final String frontendBaseUrl;

	public OAuth2LoginFailureHandler(@Value("${agenthub.frontend.base-url}") String frontendBaseUrl) {
		this.frontendBaseUrl = frontendBaseUrl;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
		throws IOException {
		response.sendRedirect(frontendBaseUrl + "/login?oauthError=" + encode("소셜 로그인에 실패했습니다."));
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
