package com.yongoh.agenthub_backend.repository.client;

import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.UUID;

import com.yongoh.agenthub_backend.global.config.AgentTraceProperties;

@Component
public class AgentTraceClient {
    private final RestClient restClient;

    public AgentTraceClient(AgentTraceProperties properties) {
        this.restClient = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .build();
    }

    public void triggerAnalysis(UUID analysisId, UUID repositoryId, UUID snapshotId, String commitSha, String githubUrl) {
        restClient.post()
            .uri("/api/v1/analysis")
            .body(Map.of(
                "analysis_id", analysisId.toString(),
                "repository_id", repositoryId.toString(),
                "snapshot_id", snapshotId.toString(),
                "commit_sha", commitSha != null ? commitSha : "",
                "github_url", githubUrl != null ? githubUrl : ""
            ))
            .retrieve()
            .toBodilessEntity();
    }
}
