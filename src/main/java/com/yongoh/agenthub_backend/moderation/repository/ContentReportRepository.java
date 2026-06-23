package com.yongoh.agenthub_backend.moderation.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.moderation.model.ContentReport;
import com.yongoh.agenthub_backend.moderation.model.ReportTargetType;
import com.yongoh.agenthub_backend.user.model.User;

public interface ContentReportRepository extends JpaRepository<ContentReport, UUID> {
	Optional<ContentReport> findByReporterAndTargetTypeAndTargetId(User reporter, ReportTargetType targetType, UUID targetId);
}
