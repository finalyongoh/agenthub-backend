package com.yongoh.agenthub_backend.repository.client;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

import com.yongoh.agenthub_backend.global.config.AgentTraceProperties;
import tools.jackson.databind.ObjectMapper;

@Component
public class AgentTraceClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AgentTraceClient(AgentTraceProperties properties, ObjectMapper objectMapper) {
        this(RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory()), properties, objectMapper);
    }

    AgentTraceClient(RestClient.Builder restClientBuilder, AgentTraceProperties properties, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder
            .baseUrl(properties.getBaseUrl())
            .build();
        this.objectMapper = objectMapper;
    }

    public void triggerAnalysis(UUID analysisId, UUID repositoryId, UUID snapshotId, String commitSha, String githubUrl) {
        String requestJson = objectMapper.writeValueAsString(Map.of(
            "analysis_id", analysisId.toString(),
            "repository_id", repositoryId.toString(),
            "snapshot_id", snapshotId.toString(),
            "commit_sha", commitSha != null ? commitSha : "",
            "github_url", githubUrl != null ? githubUrl : ""
        ));

        restClient.post()
            .uri("/api/v1/analysis")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(requestJson)
            .retrieve()
            .toBodilessEntity();
    }
}
