package com.yongoh.agenthub_backend.repository.repository;

import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryAnalysisRepository extends JpaRepository<RepositoryAnalysis, UUID> {
    Optional<RepositoryAnalysis> findFirstByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);
    boolean existsByRepositoryId(UUID repositoryId);
}
