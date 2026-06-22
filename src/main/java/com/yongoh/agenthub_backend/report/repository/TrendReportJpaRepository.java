package com.yongoh.agenthub_backend.report.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.report.model.TrendReport;
import com.yongoh.agenthub_backend.report.model.TrendReportStatus;

public interface TrendReportJpaRepository extends JpaRepository<TrendReport, UUID> {
	Optional<TrendReport> findByPeriodStartAndPeriodEnd(LocalDate periodStart, LocalDate periodEnd);

	Optional<TrendReport> findFirstByStatusOrderByPeriodEndDesc(TrendReportStatus status);

	Page<TrendReport> findByStatusOrderByPeriodEndDesc(TrendReportStatus status, Pageable pageable);
}
