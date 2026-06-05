package com.yongoh.agenthub_backend.repository.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.repository.model.RepositoryChangeLog;

public interface RepositoryChangeLogJpaRepository extends JpaRepository<RepositoryChangeLog, UUID> {
}
