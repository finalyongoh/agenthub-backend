package com.yongoh.agenthub_backend.github.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubRepositoryDto {
	private Long githubId;
	private String fullName;
	private String owner;
	private String name;
	private String description;
	private String htmlUrl;
	private String cloneUrl;
	private String homepage;
	private String defaultBranch;
	private String language;
	private List<String> topics;
	private int stars;
	private int forks;
	private int watchers;
	private int openIssues;
	private String license;
	private Instant pushedAt;
	private Instant githubCreatedAt;
	private Instant githubUpdatedAt;
	private boolean archived;
	private boolean fork;
}
