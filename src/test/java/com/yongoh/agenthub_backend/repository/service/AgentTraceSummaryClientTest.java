package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.yongoh.agenthub_backend.global.config.AgentTraceProperties;
import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryFileTree;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;
import tools.jackson.databind.ObjectMapper;

class AgentTraceSummaryClientTest {
	@Test
	void summarizePostsRepositorySnapshotToAgentTrace() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AgentTraceProperties properties = new AgentTraceProperties();
		AgentTraceSummaryClient client = new AgentTraceSummaryClient(builder, properties, new ObjectMapper());
		AgentRepository repository = AgentRepository.create(repositoryDto(1L, "acme/weather-agent"));
		RepositoryReadme readme = RepositoryReadme.create(repository, "README.md", "readme-sha", "# Weather Agent", 15, false);
		RepositoryFileTree fileTree = RepositoryFileTree.create(repository, "[{\"path\":\"README.md\",\"type\":\"file\"}]", 1);

		server.expect(requestTo("http://localhost:8000/v1/repository-summaries"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.repository.repository_id").value(repository.getId().toString()))
			.andExpect(jsonPath("$.repository.full_name").value("acme/weather-agent"))
			.andExpect(jsonPath("$.repository.github_url").value("https://github.com/acme/weather-agent"))
			.andExpect(jsonPath("$.repository.topics[0]").value("ai-agent"))
			.andExpect(jsonPath("$.readme_text").value("# Weather Agent"))
			.andExpect(jsonPath("$.shallow_file_tree[0]").value("README.md"))
			.andRespond(withSuccess("""
				{
				  "repository_id": "repo-1",
				  "full_name": "acme/weather-agent",
				  "github_url": "https://github.com/acme/weather-agent",
				  "summary_status": "completed",
				  "readme_summary": "AgentTrace summary"
				}
				""", MediaType.APPLICATION_JSON));

		AgentTraceSummaryClient.RepositorySummaryResult result = client.summarize(repository, readme, fileTree);

		assertThat(result.completed()).isTrue();
		assertThat(result.readmeSummary()).isEqualTo("AgentTrace summary");
		server.verify();
	}

	@Test
	void summarizePreservesAgentTraceValidationErrorBody() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AgentTraceProperties properties = new AgentTraceProperties();
		AgentTraceSummaryClient client = new AgentTraceSummaryClient(builder, properties, new ObjectMapper());
		AgentRepository repository = AgentRepository.create(repositoryDto(2L, "acme/bad-agent"));
		RepositoryReadme readme = RepositoryReadme.create(repository, "README.md", "readme-sha", "# Bad Agent", 11, false);
		RepositoryFileTree fileTree = RepositoryFileTree.create(repository, "[{\"path\":\"README.md\",\"type\":\"file\"}]", 1);

		server.expect(requestTo("http://localhost:8000/v1/repository-summaries"))
			.andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
					{"detail":[{"loc":["body","repository","topics",0],"msg":"Input should be a valid string","input":123}]}
					"""));

		try {
			client.summarize(repository, readme, fileTree);
		} catch (AgentTraceSummaryException exception) {
			assertThat(exception.getStatusCode()).isEqualTo(422);
			assertThat(exception.getResponseBody()).contains("\"loc\":[\"body\",\"repository\",\"topics\",0]");
			assertThat(exception.getResponseBody()).contains("\"input\":123");
		}

		server.verify();
	}

	private GithubRepositoryDto repositoryDto(Long id, String fullName) {
		String[] parts = fullName.split("/");
		return new GithubRepositoryDto(
			id,
			fullName,
			parts[0],
			parts[1],
			"Agent repository",
			"https://github.com/" + fullName,
			"https://github.com/" + fullName + ".git",
			null,
			"main",
			"Java",
			List.of("ai-agent"),
			100,
			5,
			8,
			1,
			"MIT",
			Instant.parse("2026-01-01T00:00:00Z"),
			Instant.parse("2025-01-01T00:00:00Z"),
			Instant.parse("2026-01-01T00:00:00Z"),
			false,
			false
		);
	}
}
