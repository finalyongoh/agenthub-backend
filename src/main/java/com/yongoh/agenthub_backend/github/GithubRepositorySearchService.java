package com.yongoh.agenthub_backend.github;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.global.config.GithubProperties;

@Service
public class GithubRepositorySearchService {
	private final GithubClient githubClient;
	private final GithubProperties properties;

	public GithubRepositorySearchService(GithubClient githubClient, GithubProperties properties) {
		this.githubClient = githubClient;
		this.properties = properties;
	}

	public List<GithubRepositoryDto> searchAgentRepositories(int limit) {
		Map<String, GithubRepositoryDto> repositories = new LinkedHashMap<>();
		int perQueryLimit = Math.max(1, Math.min(100, limit));
		for (String query : properties.getSync().getQueries()) {
			for (GithubRepositoryDto repository : githubClient.searchRepositories(query, perQueryLimit)) {
				repositories.putIfAbsent(repository.getFullName(), repository);
				if (repositories.size() >= limit) {
					return List.copyOf(repositories.values());
				}
			}
		}
		return List.copyOf(repositories.values());
	}
}
