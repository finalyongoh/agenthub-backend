package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.yongoh.agenthub_backend.repository.client.AgentTraceClient;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;

@SpringBootTest
@Transactional
class AnalysisServiceTest {

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private RepositoryAnalysisRepository repositoryAnalysisRepository;

    @MockitoBean
    private AgentTraceClient agentTraceClient;

    @Test
    void testRequestAnalysisAndLifecycle() {
        UUID repositoryId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        String commitSha = "abc123commit";
        String githubUrl = "https://github.com/example/repo";

        // 1. Request analysis
        RepositoryAnalysis analysis = analysisService.requestAnalysis(repositoryId, snapshotId, commitSha, githubUrl);

        // Verify entity in DB
        assertThat(analysis).isNotNull();
        assertThat(analysis.getAnalysisId()).isNotNull();
        assertThat(analysis.getRepositoryId()).isEqualTo(repositoryId);
        assertThat(analysis.getSnapshotId()).isEqualTo(snapshotId);
        assertThat(analysis.getStatus()).isEqualTo("PROCESSING");
        assertThat(analysis.getCreatedAt()).isNotNull();
        assertThat(analysis.getUpdatedAt()).isNotNull();

        // Verify client call
        verify(agentTraceClient).triggerAnalysis(
            eq(analysis.getAnalysisId()),
            eq(repositoryId),
            eq(snapshotId),
            eq(commitSha),
            eq(githubUrl)
        );

        // Fetch from DB to confirm persistence
        RepositoryAnalysis persisted = repositoryAnalysisRepository.findById(analysis.getAnalysisId()).orElse(null);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getStatus()).isEqualTo("PROCESSING");

        // 2. Update status to COMPLETED
        String resultJson = "{\"result\":\"success\"}";
        analysisService.updateStatus(analysis.getAnalysisId(), "COMPLETED", resultJson, null);

        // Verify updated entity in DB
        RepositoryAnalysis updated = repositoryAnalysisRepository.findById(analysis.getAnalysisId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo("COMPLETED");
        assertThat(updated.getResultJson()).isEqualTo(resultJson);
        assertThat(updated.getErrorMessage()).isNull();
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());

        // 3. Update status with error
        analysisService.updateStatus(analysis.getAnalysisId(), "FAILED", null, "Something went wrong");

        RepositoryAnalysis failed = repositoryAnalysisRepository.findById(analysis.getAnalysisId()).orElse(null);
        assertThat(failed).isNotNull();
        assertThat(failed.getStatus()).isEqualTo("FAILED");
        assertThat(failed.getResultJson()).isEqualTo(resultJson); // should preserve old value based on updateStatus implementation
        assertThat(failed.getErrorMessage()).isEqualTo("Something went wrong");
    }

    @Test
    void testRequestAnalysisFailureFlow() {
        UUID repositoryId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        String commitSha = "error-commit";
        String githubUrl = "https://github.com/example/repo-err";

        // Stub the client to throw an exception
        org.mockito.Mockito.doThrow(new RuntimeException("API connection failure"))
            .when(agentTraceClient).triggerAnalysis(any(), any(), any(), any(), any());

        // Request analysis and expect exception
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
            analysisService.requestAnalysis(repositoryId, snapshotId, commitSha, githubUrl)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("API connection failure");

        // Verify that status in DB was updated to FAILED with the error message
        RepositoryAnalysis analysis = repositoryAnalysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId).orElse(null);
        assertThat(analysis).isNotNull();
        assertThat(analysis.getStatus()).isEqualTo("FAILED");
        assertThat(analysis.getErrorMessage()).isEqualTo("API connection failure");
    }
}
