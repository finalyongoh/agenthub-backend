package com.yongoh.agenthub_backend.repository.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryFileTree;

public interface RepositoryFileTreeJpaRepository extends JpaRepository<RepositoryFileTree, UUID> {
	Optional<RepositoryFileTree> findByRepository(AgentRepository repository);
}
