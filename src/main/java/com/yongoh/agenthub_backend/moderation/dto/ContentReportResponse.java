package com.yongoh.agenthub_backend.moderation.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.moderation.model.ContentReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentReportResponse {
	private UUID id;
	private String targetType;
	private UUID targetId;
	private String category;
	private String reason;
	private String status;
	private Instant createdAt;

	public static ContentReportResponse from(ContentReport report) {
		return new ContentReportResponse(
			report.getId(),
			report.getTargetType().name(),
			report.getTargetId(),
			report.getCategory(),
			report.getReason(),
			report.getStatus().name(),
			report.getCreatedAt()
		);
	}
}
