package com.yongoh.agenthub_backend.user.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.user.model.User;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class JwtUtil {
	private static final String HMAC_SHA256 = "HmacSHA256";

	private final String issuer;
	private final Duration accessTokenTtl;
	private final String secret;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	@Autowired
	public JwtUtil(
		@Value("${agenthub.auth.jwt.issuer}") String issuer,
		@Value("${agenthub.auth.jwt.access-token-ttl}") Duration accessTokenTtl,
		@Value("${agenthub.auth.jwt.secret}") String secret,
		ObjectMapper objectMapper
	) {
		this(issuer, accessTokenTtl, secret, objectMapper, Clock.systemUTC());
	}

	JwtUtil(String issuer, Duration accessTokenTtl, String secret, ObjectMapper objectMapper, Clock clock) {
		this.issuer = issuer;
		this.accessTokenTtl = accessTokenTtl;
		this.secret = secret;
		this.objectMapper = objectMapper;
		this.clock = clock;
	}

	public String createAccessToken(User user) {
		return createToken(user, "access", accessTokenTtl.getSeconds());
	}

	public JwtClaims parse(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw invalidToken();
		}
		String signatureSource = parts[0] + "." + parts[1];
		String expectedSignature = base64Url(hmac(signatureSource));
		// 서명 비교는 timing attack을 줄이기 위해 constant-time 비교를 사용한다.
		if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
			throw invalidToken();
		}
		try {
			JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
			Instant expiresAt = Instant.ofEpochSecond(payload.path("exp").asLong());
			if (!expiresAt.isAfter(clock.instant())) {
				throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "토큰이 만료되었습니다.");
			}
			return new JwtClaims(
				UUID.fromString(payload.path("sub").asText()),
				payload.path("typ").asText(),
				payload.path("role").asText(),
				expiresAt
			);
		} catch (ApiException exception) {
			throw exception;
		} catch (Exception exception) {
			throw invalidToken();
		}
	}

	private String createToken(User user, String type, long ttlSeconds) {
		Instant now = clock.instant();
		Instant expiresAt = now.plusSeconds(ttlSeconds);

		// 외부 라이브러리 없이 현재 필요한 HS256 access token만 생성한다.
		ObjectNode header = objectMapper.createObjectNode();
		header.put("alg", "HS256");
		header.put("typ", "JWT");

		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("iss", issuer);
		payload.put("sub", user.getId().toString());
		payload.put("typ", type);
		payload.put("role", user.getRole().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", expiresAt.getEpochSecond());

		try {
			String headerPart = base64Url(objectMapper.writeValueAsBytes(header));
			String payloadPart = base64Url(objectMapper.writeValueAsBytes(payload));
			String signatureSource = headerPart + "." + payloadPart;
			return signatureSource + "." + base64Url(hmac(signatureSource));
		} catch (Exception exception) {
			throw new IllegalStateException("JWT 생성에 실패했습니다.", exception);
		}
	}

	private byte[] hmac(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
			return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("JWT 서명에 실패했습니다.", exception);
		}
	}

	private static String base64Url(byte[] value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
	}

	private static ApiException invalidToken() {
		return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "유효하지 않은 토큰입니다.");
	}

	public static class JwtClaims {
		private final UUID userId;
		private final String type;
		private final String role;
		private final Instant expiresAt;

		public JwtClaims(UUID userId, String type, String role, Instant expiresAt) {
			this.userId = userId;
			this.type = type;
			this.role = role;
			this.expiresAt = expiresAt;
		}

		public UUID getUserId() {
			return userId;
		}

		public String getType() {
			return type;
		}

		public String getRole() {
			return role;
		}

		public Instant getExpiresAt() {
			return expiresAt;
		}
	}
}
