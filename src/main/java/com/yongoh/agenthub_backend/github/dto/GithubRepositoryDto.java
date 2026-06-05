package com.yongoh.agenthub_backend.github.dto;

import java.time.Instant;
import java.util.List;

public record GithubRepositoryDto(
	Long githubId,
	String fullName,
	String owner,
	String name,
	String description,
	String htmlUrl,
	String cloneUrl,
	String homepage,
	String defaultBranch,
	String language,
	List<String> topics,
	int stars,
	int forks,
	int watchers,
	int openIssues,
	String license,
	Instant pushedAt,
	Instant githubCreatedAt,
	Instant githubUpdatedAt,
	boolean archived,
	boolean fork
) {
}
