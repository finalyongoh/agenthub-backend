package com.yongoh.agenthub_backend.report.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "trend_reports",
	uniqueConstraints = @UniqueConstraint(name = "uk_trend_report_period", columnNames = {"period_start", "period_end"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendReport {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "period_start", nullable = false)
	private LocalDate periodStart;

	@Column(name = "period_end", nullable = false)
	private LocalDate periodEnd;

	@Column(nullable = false, length = 300)
	private String title;

	@Column(name = "content_json", columnDefinition = "text", nullable = false)
	private String contentJson;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TrendReportStatus status;

	@Column(name = "model_name", length = 100)
	private String modelName;

	@Column(name = "prompt_version", length = 100)
	private String promptVersion;

	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;

	@Column(name = "generated_at", nullable = false)
	private Instant generatedAt;

	public static TrendReport published(LocalDate start, LocalDate end, String title, String contentJson, String modelName, String promptVersion) {
		TrendReport report = new TrendReport();
		report.id = UUID.randomUUID();
		report.periodStart = start;
		report.periodEnd = end;
		report.title = title;
		report.contentJson = contentJson;
		report.status = TrendReportStatus.PUBLISHED;
		report.modelName = modelName;
		report.promptVersion = promptVersion;
		report.generatedAt = Instant.now();
		return report;
	}
}
