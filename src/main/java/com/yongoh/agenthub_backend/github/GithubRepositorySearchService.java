package com.yongoh.agenthub_backend.github;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.global.config.GithubProperties;

@Service
public class GithubRepositorySearchService {
	private static final Logger log = LoggerFactory.getLogger(GithubRepositorySearchService.class);

	private final GithubClient githubClient;
	private final GithubProperties properties;

	public GithubRepositorySearchService(GithubClient githubClient, GithubProperties properties) {
		this.githubClient = githubClient;
		this.properties = properties;
	}

	public List<GithubRepositoryDto> searchAgentRepositories(int limit) {
		Map<String, GithubRepositoryDto> repositories = new LinkedHashMap<>();
		int perQueryLimit = Math.max(1, Math.min(100, limit));
		for (String seedRepository : properties.getSync().getSeedRepositories()) {
			try {
				githubClient.findRepository(seedRepository)
					.ifPresent(repository -> repositories.putIfAbsent(repository.getFullName(), repository));
			} catch (GithubApiException exception) {
				log.warn("Seed repository lookup failed: repository={}, status={}", seedRepository, exception.getStatusCode());
			}
			if (repositories.size() >= limit) {
				return List.copyOf(repositories.values());
			}
		}
		for (String query : properties.getSync().getQueries()) {
			for (GithubRepositoryDto repository : githubClient.searchRepositories(query, perQueryLimit)) {
				if (!shouldKeepSearchResult(repository)) {
					continue;
				}
				repositories.putIfAbsent(repository.getFullName(), repository);
				if (repositories.size() >= limit) {
					return List.copyOf(repositories.values());
				}
			}
		}
		return List.copyOf(repositories.values());
	}

	private boolean shouldKeepSearchResult(GithubRepositoryDto repository) {
		String target = normalize(
			repository.getFullName()
				+ " "
				+ repository.getName()
				+ " "
				+ repository.getDescription()
				+ " "
				+ repository.getTopics()
				+ " "
				+ repository.getLanguage()
		);
		boolean strongAiAgentSignal = containsAny(
			target,
			"ai-agent",
			"ai agent",
			"llm",
			"large language model",
			"agentic",
			"autonomous agent",
			"multi-agent",
			"rag",
			"retrieval augmented generation",
			"model context protocol",
			"mcp",
			"langchain",
			"langgraph",
			"crewai",
			"autogen",
			"pydantic-ai",
			"openai agents",
			"semantic kernel"
		);
		boolean aiMlSignal = containsAny(
			target,
			"machine learning",
			"deep learning",
			"natural language processing",
			"nlp",
			"computer vision",
			"model serving",
			"inference",
			"transformer",
			"diffusion"
		);
		if (isResourceCatalog(target)) {
			return strongAiAgentSignal;
		}
		return strongAiAgentSignal || aiMlSignal;
	}

	private boolean isResourceCatalog(String target) {
		return containsAny(
			target,
			"awesome",
			"roadmap",
			"free-for-dev",
			"free for dev",
			"public api",
			"public-apis",
			"system prompt",
			"prompts",
			"guide",
			"tutorial",
			"interview",
			"algorithm"
		);
	}

	private boolean containsAny(String target, String... needles) {
		for (String needle : needles) {
			if (target.contains(normalize(needle))) {
				return true;
			}
		}
		return false;
	}

	private String normalize(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}
}
