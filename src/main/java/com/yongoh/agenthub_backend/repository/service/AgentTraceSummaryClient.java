package com.yongoh.agenthub_backend.repository.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.yongoh.agenthub_backend.global.config.AgentTraceProperties;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryFileTree;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class AgentTraceSummaryClient {
	private static final Logger log = LoggerFactory.getLogger(AgentTraceSummaryClient.class);
	private static final String ENDPOINT = "/v1/repository-summaries";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	@Autowired
	public AgentTraceSummaryClient(AgentTraceProperties properties, ObjectMapper objectMapper) {
		this(RestClient.builder().requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory()), properties, objectMapper);
	}

	AgentTraceSummaryClient(RestClient.Builder restClientBuilder, AgentTraceProperties properties, ObjectMapper objectMapper) {
		this.restClient = restClientBuilder
			.baseUrl(properties.getBaseUrl())
			.build();
		this.objectMapper = objectMapper;
	}

	public RepositorySummaryResult summarize(
		AgentRepository repository,
		RepositoryReadme readme,
		RepositoryFileTree fileTree
	) {
		try {
			Map<String, Object> request = request(repository, readme, fileTree);
			String requestJson = objectMapper.writeValueAsString(request);
			log.info("AgentTrace repository summary request payload={}", requestJson);
			Map<String, Object> response = restClient.post()
				.uri(ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(requestJson)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {});
			return result(response);
		} catch (RestClientResponseException exception) {
			String responseBody = exception.getResponseBodyAsString();
			log.warn(
				"AgentTrace repository summary request failed: status={}, body={}",
				exception.getStatusCode().value(),
				responseBody
			);
			throw new AgentTraceSummaryException(
				"AgentTrace repository summary request failed.",
				exception.getStatusCode().value(),
				responseBody,
				exception
			);
		} catch (RuntimeException exception) {
			throw new AgentTraceSummaryException("AgentTrace repository summary request failed.", exception);
		}
	}

	private Map<String, Object> request(
		AgentRepository repository,
		RepositoryReadme readme,
		RepositoryFileTree fileTree
	) {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("repository", repositoryMetadata(repository));
		request.put("snapshot_id", readme.getSha());
		request.put("readme_text", readme.getContent());
		request.put("shallow_file_tree", fileTreePaths(fileTree));
		request.put("options", Map.of());
		return request;
	}

	private Map<String, Object> repositoryMetadata(AgentRepository repository) {
		return Map.ofEntries(
			Map.entry("repository_id", repository.getId().toString()),
			Map.entry("full_name", repository.getFullName()),
			Map.entry("github_url", valueOrEmpty(repository.getHtmlUrl())),
			Map.entry("description", valueOrEmpty(repository.getDescription())),
			Map.entry("topics", topics(repository.getTopics())),
			Map.entry("primary_language", valueOrEmpty(repository.getLanguage())),
			Map.entry("stars", repository.getStars()),
			Map.entry("forks", repository.getForks()),
			Map.entry("pushed_at", instant(repository.getPushedAt())),
			Map.entry("github_updated_at", instant(repository.getGithubUpdatedAt()))
		);
	}

	private List<String> topics(String topics) {
		if (!StringUtils.hasText(topics)) {
			return List.of();
		}
		return Arrays.stream(topics.split(","))
			.map(String::trim)
			.filter(StringUtils::hasText)
			.toList();
	}

	private List<String> fileTreePaths(RepositoryFileTree fileTree) {
		if (fileTree == null || !StringUtils.hasText(fileTree.getTreeJson())) {
			return List.of();
		}
		try {
			List<Map<String, Object>> entries = objectMapper.readValue(
				fileTree.getTreeJson(),
				new TypeReference<>() {}
			);
			return entries.stream()
				.map(entry -> entry.get("path"))
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.filter(StringUtils::hasText)
				.toList();
		} catch (RuntimeException exception) {
			throw new AgentTraceSummaryException("Repository file tree JSON could not be parsed.", exception);
		}
	}

	private RepositorySummaryResult result(Map<String, Object> response) {
		String status = stringValue(response, "summary_status");
		String readmeSummary = stringValue(response, "readme_summary");
		if (!StringUtils.hasText(readmeSummary)) {
			readmeSummary = stringValue(response, "one_line_summary");
		}
		String errorMessage = stringValue(response, "error_message");
		return new RepositorySummaryResult("completed".equals(status), readmeSummary, errorMessage);
	}

	private String stringValue(Map<String, Object> response, String key) {
		Object value = response == null ? null : response.get(key);
		return value == null ? null : String.valueOf(value);
	}

	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	private String instant(Instant instant) {
		return instant == null ? "" : instant.toString();
	}

	public record RepositorySummaryResult(boolean completed, String readmeSummary, String errorMessage) {
	}
}
