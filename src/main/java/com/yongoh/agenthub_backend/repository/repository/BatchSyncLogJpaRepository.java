package com.yongoh.agenthub_backend.repository.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.repository.model.BatchSyncLog;
import com.yongoh.agenthub_backend.repository.model.BatchSyncLogStatus;

public interface BatchSyncLogJpaRepository extends JpaRepository<BatchSyncLog, UUID> {
	Page<BatchSyncLog> findByStatus(BatchSyncLogStatus status, Pageable pageable);
}
