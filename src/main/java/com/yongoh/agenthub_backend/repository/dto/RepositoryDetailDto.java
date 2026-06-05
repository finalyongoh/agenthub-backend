package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;

public record RepositoryDetailDto(
	UUID id,
	String fullName,
	String name,
	String owner,
	String description,
	String htmlUrl,
	String cloneUrl,
	String homepage,
	String language,
	List<String> topics,
	int stars,
	int forks,
	int watchers,
	int openIssues,
	String license,
	Instant pushedAt,
	int agentScore,
	String agentCategory,
	String readmeSummary,
	String readmePreview,
	String analysisStatus
) {
	public static RepositoryDetailDto from(AgentRepository repository, RepositoryReadme readme, RepositoryAnalysis analysis) {
		RepositorySummaryDto summary = RepositorySummaryDto.from(repository, analysis != null);
		String content = readme == null || readme.getContent() == null ? "" : readme.getContent();
		String preview = content.length() > 4000 ? content.substring(0, 4000) : content;
		return new RepositoryDetailDto(
			repository.getId(),
			repository.getFullName(),
			repository.getName(),
			repository.getOwner(),
			repository.getDescription(),
			repository.getHtmlUrl(),
			repository.getCloneUrl(),
			repository.getHomepage(),
			repository.getLanguage(),
			summary.topics(),
			repository.getStars(),
			repository.getForks(),
			repository.getWatchers(),
			repository.getOpenIssues(),
			repository.getLicense(),
			repository.getPushedAt(),
			repository.getAgentScore(),
			repository.getAgentCategory(),
			repository.getReadmeSummary(),
			preview,
			analysis == null ? null : analysis.getStatus().name().toLowerCase()
		);
	}
}
