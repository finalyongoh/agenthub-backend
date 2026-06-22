package com.yongoh.agenthub_backend.report.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;

@Getter
public class TrendReportResponse {
	private final UUID id;
	private final LocalDate periodStart;
	private final LocalDate periodEnd;
	private final String title;
	private final Map<String, Object> content;
	private final Instant generatedAt;

	public TrendReportResponse(UUID id, LocalDate periodStart, LocalDate periodEnd, String title, Map<String, Object> content, Instant generatedAt) {
		this.id = id;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.title = title;
		this.content = content;
		this.generatedAt = generatedAt;
	}
}
