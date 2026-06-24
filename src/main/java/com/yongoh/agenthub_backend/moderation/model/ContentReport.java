package com.yongoh.agenthub_backend.moderation.model;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "content_reports",
	indexes = {
		@Index(name = "idx_content_reports_target", columnList = "target_type, target_id"),
		@Index(name = "idx_content_reports_reporter", columnList = "reporter_id"),
		@Index(name = "idx_content_reports_status_created", columnList = "status, created_at")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentReport {
	@Id
	@Column(name = "report_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reporter_id", nullable = false)
	private User reporter;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 40)
	private ReportTargetType targetType;

	@Column(name = "target_id", nullable = false)
	private UUID targetId;

	@Column(nullable = false, length = 80)
	private String category;

	@Column(nullable = false, columnDefinition = "text")
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ContentReportStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static ContentReport create(User reporter, ReportTargetType targetType, UUID targetId, String category, String reason) {
		ContentReport report = new ContentReport();
		report.id = UUID.randomUUID();
		report.reporter = reporter;
		report.targetType = targetType;
		report.targetId = targetId;
		report.category = category;
		report.reason = reason;
		report.status = ContentReportStatus.PENDING;
		return report;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
