package com.yongoh.agenthub_backend.repository.service;

import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.repository.client.AgentTraceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final RepositoryAnalysisRepository repository;
    private final AgentTraceClient client;

    @Transactional
    public RepositoryAnalysis requestAnalysis(UUID repositoryId, UUID snapshotId, String commitSha, String githubUrl) {
        RepositoryAnalysis analysis = RepositoryAnalysis.builder()
            .repositoryId(repositoryId)
            .snapshotId(snapshotId)
            .status("QUEUED")
            .build();
        
        RepositoryAnalysis saved = repository.save(analysis);
        client.triggerAnalysis(saved.getAnalysisId(), repositoryId, snapshotId, commitSha, githubUrl);
        return saved;
    }

    @Transactional
    public void updateStatus(UUID analysisId, String status, String resultJson, String errorMessage) {
        RepositoryAnalysis analysis = repository.findById(analysisId)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));
        analysis.setStatus(status);
        if (resultJson != null) analysis.setResultJson(resultJson);
        if (errorMessage != null) analysis.setErrorMessage(errorMessage);
        repository.save(analysis);
    }
}
