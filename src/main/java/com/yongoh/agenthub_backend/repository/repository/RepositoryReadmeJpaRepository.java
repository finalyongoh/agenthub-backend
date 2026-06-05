package com.yongoh.agenthub_backend.repository.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryReadme;

public interface RepositoryReadmeJpaRepository extends JpaRepository<RepositoryReadme, UUID> {
	Optional<RepositoryReadme> findByRepository(AgentRepository repository);
}
