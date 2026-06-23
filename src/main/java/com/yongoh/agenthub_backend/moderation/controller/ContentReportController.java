package com.yongoh.agenthub_backend.moderation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yongoh.agenthub_backend.global.security.AuthenticatedUser;
import com.yongoh.agenthub_backend.moderation.dto.ContentReportRequest;
import com.yongoh.agenthub_backend.moderation.dto.ContentReportResponse;
import com.yongoh.agenthub_backend.moderation.service.ContentReportService;

@RestController
@RequestMapping("/api/reports")
public class ContentReportController {
	private final ContentReportService reportService;

	public ContentReportController(ContentReportService reportService) {
		this.reportService = reportService;
	}

	@PostMapping
	public ContentReportResponse createReport(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestBody ContentReportRequest request
	) {
		return reportService.createReport(user.getId(), request);
	}
}
