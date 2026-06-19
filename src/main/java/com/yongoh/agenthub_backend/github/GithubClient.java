package com.yongoh.agenthub_backend.github;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.yongoh.agenthub_backend.github.dto.GithubReadmeDto;
import com.yongoh.agenthub_backend.github.dto.GithubFileTreeItemDto;
import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.global.config.GithubProperties;

@Component
public class GithubClient {
	private final RestClient restClient;
	private final GithubProperties properties;

	public GithubClient(GithubProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder()
			.baseUrl(properties.getApi().getBaseUrl())
			.defaultHeader("X-GitHub-Api-Version", properties.getApi().getVersion())
			.build();
	}

	public List<GithubRepositoryDto> searchRepositories(String query, int perPage) {
		URI uri = UriComponentsBuilder.fromPath("/search/repositories")
			.queryParam("q", query)
			.queryParam("sort", properties.getSync().getSearchSort())
			.queryParam("order", properties.getSync().getSearchOrder())
			.queryParam("per_page", perPage)
			.build()
			.encode()
			.toUri();

		Map<String, Object> response = get(uri, "application/vnd.github+json", Map.class);
		List<GithubRepositoryDto> repositories = new ArrayList<>();
		Object items = response.get("items");
		if (items instanceof List<?> itemList) {
			for (Object item : itemList) {
				if (item instanceof Map<?, ?> itemMap) {
					GithubRepositoryDto repository = toRepositoryDto(itemMap);
					if (!repository.isArchived() && !repository.isFork() && repository.getStars() >= properties.getSync().getMinStars()) {
						repositories.add(repository);
					}
				}
			}
		}
		return repositories;
	}

	public Optional<GithubReadmeDto> findReadme(String owner, String repo) {
		Optional<GithubReadmeDto> primary = getReadme("/repos/%s/%s/readme".formatted(owner, repo), "README");
		if (primary.isPresent()) {
			return primary;
		}
		for (String path : List.of("README.md", "README.MD", "readme.md", "README", "README.rst", "README.txt")) {
			Optional<GithubReadmeDto> fallback = getReadme("/repos/%s/%s/contents/%s".formatted(owner, repo, path), path);
			if (fallback.isPresent()) {
				return fallback;
			}
		}
		return Optional.empty();
	}

	public List<GithubFileTreeItemDto> findFileTree(String owner, String repo, String branch) {
		URI uri = UriComponentsBuilder.fromPath("/repos/{owner}/{repo}/git/trees/{branch}")
			.queryParam("recursive", 1)
			.build(owner, repo, branch);

		Map<String, Object> response = get(uri, "application/vnd.github+json", Map.class);
		List<GithubFileTreeItemDto> fileTree = new ArrayList<>();
		Object tree = response.get("tree");
		if (tree instanceof List<?> entries) {
			for (Object entry : entries) {
				if (entry instanceof Map<?, ?> entryMap) {
					fileTree.add(new GithubFileTreeItemDto(
						textOrNull(entryMap, "path"),
						textOrNull(entryMap, "type")
					));
					if (fileTree.size() >= properties.getSync().getFileTreeMaxPaths()) {
						break;
					}
				}
			}
		}
		return fileTree;
	}

	private Optional<GithubReadmeDto> getReadme(String path, String fallbackPath) {
		try {
			Map<String, Object> metadata = get(URI.create(path), "application/vnd.github+json", Map.class);
			String readmePath = textOrDefault(metadata, "path", fallbackPath);
			String sha = textOrNull(metadata, "sha");
			String content = getReadmeContent(path, metadata);
			return Optional.of(new GithubReadmeDto(readmePath, sha, content));
		} catch (GithubApiException exception) {
			if (exception.isNotFound()) {
				return Optional.empty();
			}
			throw exception;
		}
	}

	private String getReadmeContent(String path, Map<String, Object> metadata) {
		String encodedContent = textOrNull(metadata, "content");
		String encoding = textOrNull(metadata, "encoding");
		if (StringUtils.hasText(encodedContent) && "base64".equalsIgnoreCase(encoding)) {
			String normalized = encodedContent.replaceAll("\\s", "");
			return new String(Base64.getDecoder().decode(normalized), StandardCharsets.UTF_8);
		}
		return get(URI.create(path), "application/vnd.github.raw+json", String.class);
	}

	private <T> T get(URI uri, String accept, Class<T> responseType) {
		int attempts = properties.getSync().getRetryCount() + 1;
		RuntimeException lastError = null;
		for (int attempt = 1; attempt <= attempts; attempt++) {
			try {
				RestClient.RequestHeadersSpec<?> spec = restClient.get()
					.uri(uri)
					.header("Accept", accept);
				if (StringUtils.hasText(properties.getApi().getToken())) {
					spec = spec.header("Authorization", "Bearer " + properties.getApi().getToken());
				}
				return spec.retrieve().body(responseType);
			} catch (HttpClientErrorException exception) {
				throw new GithubApiException(exception.getStatusCode().value(), exception.getResponseBodyAsString());
			} catch (RuntimeException exception) {
				lastError = exception;
			}
		}
		throw lastError;
	}

	private GithubRepositoryDto toRepositoryDto(Map<?, ?> item) {
		return new GithubRepositoryDto(
			longValue(item, "id"),
			textOrNull(item, "full_name"),
			item.get("owner") instanceof Map<?, ?> owner ? textOrNull(owner, "login") : "",
			textOrNull(item, "name"),
			textOrNull(item, "description"),
			textOrNull(item, "html_url"),
			textOrNull(item, "clone_url"),
			textOrNull(item, "homepage"),
			textOrNull(item, "default_branch"),
			textOrNull(item, "language"),
			toTopics(item.get("topics")),
			intValue(item, "stargazers_count"),
			intValue(item, "forks_count"),
			intValue(item, "watchers_count"),
			intValue(item, "open_issues_count"),
			item.get("license") instanceof Map<?, ?> license ? textOrNull(license, "spdx_id") : null,
			instantOrNull(item, "pushed_at"),
			instantOrNull(item, "created_at"),
			instantOrNull(item, "updated_at"),
			booleanValue(item, "archived"),
			booleanValue(item, "fork")
		);
	}

	private List<String> toTopics(Object node) {
		List<String> topics = new ArrayList<>();
		if (node instanceof List<?> values) {
			for (Object topic : values) {
				topics.add(String.valueOf(topic));
			}
		}
		return topics;
	}

	private String textOrNull(Map<?, ?> node, String field) {
		Object value = node.get(field);
		return value == null ? null : String.valueOf(value);
	}

	private String textOrDefault(Map<?, ?> node, String field, String defaultValue) {
		String value = textOrNull(node, field);
		return StringUtils.hasText(value) ? value : defaultValue;
	}

	private Instant instantOrNull(Map<?, ?> node, String field) {
		String value = textOrNull(node, field);
		return StringUtils.hasText(value) ? Instant.parse(value) : null;
	}

	private int intValue(Map<?, ?> node, String field) {
		Object value = node.get(field);
		return value instanceof Number number ? number.intValue() : 0;
	}

	private long longValue(Map<?, ?> node, String field) {
		Object value = node.get(field);
		return value instanceof Number number ? number.longValue() : 0L;
	}

	private boolean booleanValue(Map<?, ?> node, String field) {
		Object value = node.get(field);
		return value instanceof Boolean bool && bool;
	}
}
