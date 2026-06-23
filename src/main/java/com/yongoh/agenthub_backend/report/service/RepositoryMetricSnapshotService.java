package com.yongoh.agenthub_backend.report.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yongoh.agenthub_backend.report.model.RepositoryMetricSnapshot;
import com.yongoh.agenthub_backend.report.repository.RepositoryMetricSnapshotJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;

@Service
public class RepositoryMetricSnapshotService {
	private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Seoul");

	private final AgentRepositoryJpaRepository repositoryJpaRepository;
	private final RepositoryMetricSnapshotJpaRepository snapshotJpaRepository;

	public RepositoryMetricSnapshotService(
		AgentRepositoryJpaRepository repositoryJpaRepository,
		RepositoryMetricSnapshotJpaRepository snapshotJpaRepository
	) {
		this.repositoryJpaRepository = repositoryJpaRepository;
		this.snapshotJpaRepository = snapshotJpaRepository;
	}

	@Transactional
	public int captureToday() {
		LocalDate today = LocalDate.now(REPORT_ZONE);
		int captured = 0;
		for (var repository : repositoryJpaRepository.findAll()) {
			if (!snapshotJpaRepository.existsByRepositoryAndCapturedOn(repository, today)) {
				snapshotJpaRepository.save(RepositoryMetricSnapshot.capture(repository, today));
				captured++;
			}
		}
		return captured;
	}
}
