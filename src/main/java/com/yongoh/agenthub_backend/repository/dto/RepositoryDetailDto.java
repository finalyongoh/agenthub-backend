package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryDetailDto {
	private UUID id;
	private String fullName;
	private String name;
	private String owner;
	private String description;
	private String htmlUrl;
	private String cloneUrl;
	private String homepage;
	private String language;
	private List<String> topics;
	private int stars;
	private int forks;
	private int watchers;
	private int openIssues;
	private String license;
	private Instant pushedAt;
	private int agentScore;
	private String agentCategory;
	private String readmeSummary;
	private String readmePreview;
	private String analysisStatus;

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
			summary.getTopics(),
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
			analysis == null ? null : analysis.getStatus().toLowerCase()
		);
	}
}
