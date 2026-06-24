package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositorySummaryDto {
	private UUID id;
	private String fullName;
	private String name;
	private String owner;
	private String description;
	private String descriptionKo;
	private String htmlUrl;
	private String language;
	private List<String> topics;
	private int stars;
	private int forks;
	private Instant pushedAt;
	private int agentScore;
	private String agentCategory;
	private String readmeSummary;
	private boolean hasAnalysis;

	public static RepositorySummaryDto from(AgentRepository repository, boolean hasAnalysis) {
		return new RepositorySummaryDto(
			repository.getId(),
			repository.getFullName(),
			repository.getName(),
			repository.getOwner(),
			repository.getDescription(),
			repository.getDescriptionKo(),
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
