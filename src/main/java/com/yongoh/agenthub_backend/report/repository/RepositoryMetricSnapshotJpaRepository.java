package com.yongoh.agenthub_backend.report.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.report.model.RepositoryMetricSnapshot;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;

public interface RepositoryMetricSnapshotJpaRepository extends JpaRepository<RepositoryMetricSnapshot, UUID> {
	boolean existsByRepositoryAndCapturedOn(AgentRepository repository, LocalDate capturedOn);

	Optional<RepositoryMetricSnapshot> findFirstByRepositoryAndCapturedOnLessThanEqualOrderByCapturedOnDesc(
		AgentRepository repository,
		LocalDate capturedOn
	);
}
