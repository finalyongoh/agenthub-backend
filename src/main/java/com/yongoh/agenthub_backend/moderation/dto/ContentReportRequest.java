package com.yongoh.agenthub_backend.moderation.dto;

import java.util.UUID;

import com.yongoh.agenthub_backend.moderation.model.ReportTargetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentReportRequest {
	private ReportTargetType targetType;
	private UUID targetId;
	private String category;
	private String reason;
}
