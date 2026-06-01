package com.yongoh.agenthub_backend.global.response;

import java.util.Map;

public class ApiErrorResponse {
	private ErrorBody error;

	public ApiErrorResponse() {
	}

	public ApiErrorResponse(ErrorBody error) {
		this.error = error;
	}

	public static ApiErrorResponse of(String code, String message) {
		return new ApiErrorResponse(new ErrorBody(code, message, Map.of()));
	}

	public ErrorBody getError() {
		return error;
	}

	public void setError(ErrorBody error) {
		this.error = error;
	}

	public static class ErrorBody {
		private String code;
		private String message;
		private Map<String, Object> details;

		public ErrorBody() {
		}

		public ErrorBody(String code, String message, Map<String, Object> details) {
			this.code = code;
			this.message = message;
			this.details = details;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Map<String, Object> getDetails() {
			return details;
		}

		public void setDetails(Map<String, Object> details) {
			this.details = details;
		}
	}
}
