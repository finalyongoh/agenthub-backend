package com.yongoh.agenthub_backend.report.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yongoh.agenthub_backend.report.dto.TrendReportResponse;
import com.yongoh.agenthub_backend.report.service.TrendReportService;

@RestController
@RequestMapping("/api/trend-reports")
public class TrendReportController {
	private final TrendReportService trendReportService;

	public TrendReportController(TrendReportService trendReportService) {
		this.trendReportService = trendReportService;
	}

	@GetMapping("/latest")
	public TrendReportResponse latest() {
		return trendReportService.latest();
	}

	@GetMapping
	public List<TrendReportResponse> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return trendReportService.list(page, size);
	}

	@PostMapping("/generate")
	public TrendReportResponse generate() {
		return trendReportService.generateLatestCompletedWeek();
	}
}
