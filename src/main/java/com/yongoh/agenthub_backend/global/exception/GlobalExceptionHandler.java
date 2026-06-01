package com.yongoh.agenthub_backend.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yongoh.agenthub_backend.global.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ApiException.class)
	ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
		return ResponseEntity
			.status(exception.status())
			.body(ApiErrorResponse.of(exception.code(), exception.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
		return ResponseEntity
			.status(HttpStatus.FORBIDDEN)
			.body(ApiErrorResponse.of("AUTH_403", "권한이 없습니다."));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		return ResponseEntity
			.badRequest()
			.body(ApiErrorResponse.of("COMMON_400", "잘못된 요청입니다."));
	}

	public static class ApiException extends RuntimeException {
		private final HttpStatus status;
		private final String code;

		public ApiException(HttpStatus status, String code, String message) {
			super(message);
			this.status = status;
			this.code = code;
		}

		public HttpStatus status() {
			return status;
		}

		public String code() {
			return code;
		}
	}
}
