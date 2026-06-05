package com.yongoh.agenthub_backend.repository.dto;

public record GithubSyncResponse(String jobName, Long jobExecutionId, String status) {
}
