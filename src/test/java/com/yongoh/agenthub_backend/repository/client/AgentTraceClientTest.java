package com.yongoh.agenthub_backend.repository.client;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.yongoh.agenthub_backend.global.config.AgentTraceProperties;
import tools.jackson.databind.ObjectMapper;

class AgentTraceClientTest {
	@Test
	void triggerAnalysisSendsJsonRequestBody() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AgentTraceProperties properties = new AgentTraceProperties();
		AgentTraceClient client = new AgentTraceClient(builder, properties, new ObjectMapper());
		UUID analysisId = UUID.randomUUID();
		UUID repositoryId = UUID.randomUUID();
		UUID snapshotId = UUID.randomUUID();

		server.expect(requestTo("http://localhost:8000/api/v1/analysis"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.analysis_id").value(analysisId.toString()))
			.andExpect(jsonPath("$.repository_id").value(repositoryId.toString()))
			.andExpect(jsonPath("$.snapshot_id").value(snapshotId.toString()))
			.andExpect(jsonPath("$.commit_sha").value("abc123"))
			.andExpect(jsonPath("$.github_url").value("https://github.com/octocat/Hello-World"))
			.andRespond(withSuccess());

		client.triggerAnalysis(analysisId, repositoryId, snapshotId, "abc123", "https://github.com/octocat/Hello-World");

		server.verify();
	}
}
