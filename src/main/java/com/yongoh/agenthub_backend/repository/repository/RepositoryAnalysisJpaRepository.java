package com.yongoh.agenthub_backend.repository.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;

public interface RepositoryAnalysisJpaRepository extends JpaRepository<RepositoryAnalysis, UUID> {
	Optional<RepositoryAnalysis> findFirstByRepositoryOrderByRequestedAtDesc(AgentRepository repository);

	boolean existsByRepository(AgentRepository repository);
}
