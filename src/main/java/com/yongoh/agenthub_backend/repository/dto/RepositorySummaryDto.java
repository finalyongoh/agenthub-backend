package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;

public record RepositorySummaryDto(
	UUID id,
	String fullName,
	String name,
	String owner,
	String description,
	String htmlUrl,
	String language,
	List<String> topics,
	int stars,
	int forks,
	Instant pushedAt,
	int agentScore,
	String agentCategory,
	String readmeSummary,
	boolean hasAnalysis
) {
	public static RepositorySummaryDto from(AgentRepository repository, boolean hasAnalysis) {
		return new RepositorySummaryDto(
			repository.getId(),
			repository.getFullName(),
			repository.getName(),
			repository.getOwner(),
			repository.getDescription(),
			repository.getHtmlUrl(),
			repository.getLanguage(),
			toTopics(repository.getTopics()),
			repository.getStars(),
			repository.getForks(),
			repository.getPushedAt(),
			repository.getAgentScore(),
			repository.getAgentCategory(),
			repository.getReadmeSummary(),
			hasAnalysis
		);
	}

	private static List<String> toTopics(String topics) {
		if (topics == null || topics.isBlank()) {
			return List.of();
		}
		return Arrays.stream(topics.split(","))
			.filter(topic -> !topic.isBlank())
			.toList();
	}
}
