package com.yongoh.agenthub_backend.report.controller;

import java.util.List;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
	public TrendReportResponse generate(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd
	) {
		if (periodStart == null && periodEnd == null) {
			return trendReportService.generateLatestCompletedWeek();
		}
		if (periodStart == null || periodEnd == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "periodStart and periodEnd must be provided together.");
		}
		return trendReportService.generatePeriod(periodStart, periodEnd);
	}
}
