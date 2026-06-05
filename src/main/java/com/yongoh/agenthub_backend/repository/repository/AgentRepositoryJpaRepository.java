package com.yongoh.agenthub_backend.repository.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;

public interface AgentRepositoryJpaRepository extends JpaRepository<AgentRepository, UUID>, JpaSpecificationExecutor<AgentRepository> {
	Optional<AgentRepository> findByGithubId(Long githubId);

	Page<AgentRepository> findByAgentRelatedTrueAndArchivedFalseAndForkFalse(Pageable pageable);

	Page<AgentRepository> findAll(Specification<AgentRepository> specification, Pageable pageable);
}
