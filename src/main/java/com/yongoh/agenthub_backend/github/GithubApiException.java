package com.yongoh.agenthub_backend.github;

public class GithubApiException extends RuntimeException {
	private final int statusCode;

	public GithubApiException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public boolean isAuthenticationError() {
		return statusCode == 401;
	}

	public boolean isRateLimitError() {
		return statusCode == 403;
	}

	public boolean isNotFound() {
		return statusCode == 404;
	}
}
