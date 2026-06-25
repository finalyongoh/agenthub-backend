package com.yongoh.agenthub_backend.repository.service;

import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.repository.client.AgentTraceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final RepositoryAnalysisRepository repository;
    private final AgentTraceClient client;

    @Autowired
    @Lazy
    private AnalysisService self;

    @Transactional
    public RepositoryAnalysis saveQueuedAnalysis(UUID repositoryId, UUID snapshotId) {
        RepositoryAnalysis analysis = RepositoryAnalysis.builder()
            .repositoryId(repositoryId)
            .snapshotId(snapshotId)
            .status("PENDING")
            .build();
        return repository.save(analysis);
    }

    public RepositoryAnalysis requestAnalysis(UUID repositoryId, UUID snapshotId, String commitSha, String githubUrl) {
        RepositoryAnalysis saved = self.saveQueuedAnalysis(repositoryId, snapshotId);
        try {
            client.triggerAnalysis(saved.getAnalysisId(), repositoryId, snapshotId, commitSha, githubUrl);
            self.updateStatus(saved.getAnalysisId(), "PROCESSING", null, null);
            saved.setStatus("PROCESSING");
        } catch (Exception e) {
            self.updateStatus(saved.getAnalysisId(), "FAILED", null, e.getMessage());
            throw e;
        }
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
