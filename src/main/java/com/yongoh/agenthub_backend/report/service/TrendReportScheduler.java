package com.yongoh.agenthub_backend.report.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrendReportScheduler {
	private static final Logger log = LoggerFactory.getLogger(TrendReportScheduler.class);

	private final RepositoryMetricSnapshotService snapshotService;
	private final TrendReportService trendReportService;

	public TrendReportScheduler(RepositoryMetricSnapshotService snapshotService, TrendReportService trendReportService) {
		this.snapshotService = snapshotService;
		this.trendReportService = trendReportService;
	}

	@Scheduled(cron = "${report.snapshot.cron:0 30 3 * * *}", zone = "Asia/Seoul")
	public void captureMetrics() {
		log.info("Captured {} repository metric snapshots.", snapshotService.captureToday());
	}

	@Scheduled(cron = "${report.weekly.cron:0 0 4 * * MON}", zone = "Asia/Seoul")
	public void generateWeeklyReport() {
		trendReportService.generateLatestCompletedWeek();
	}
}
