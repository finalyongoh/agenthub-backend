package com.yongoh.agenthub_backend.report.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.yongoh.agenthub_backend.report.dto.TrendReportResponse;
import com.yongoh.agenthub_backend.report.model.RepositoryMetricSnapshot;
import com.yongoh.agenthub_backend.report.model.TrendReport;
import com.yongoh.agenthub_backend.report.model.TrendReportStatus;
import com.yongoh.agenthub_backend.report.repository.RepositoryMetricSnapshotJpaRepository;
import com.yongoh.agenthub_backend.report.repository.TrendReportJpaRepository;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisJpaRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class TrendReportService {
	private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Seoul");
	private static final int REPORT_REPOSITORY_LIMIT = 20;

	private final AgentRepositoryJpaRepository repositoryJpaRepository;
	private final RepositoryAnalysisJpaRepository analysisJpaRepository;
	private final RepositoryMetricSnapshotJpaRepository snapshotJpaRepository;
	private final TrendReportJpaRepository trendReportJpaRepository;
	private final AgentTraceTrendReportClient reportClient;
	private final ObjectMapper objectMapper;

	public TrendReportService(
		AgentRepositoryJpaRepository repositoryJpaRepository,
		RepositoryAnalysisJpaRepository analysisJpaRepository,
		RepositoryMetricSnapshotJpaRepository snapshotJpaRepository,
		TrendReportJpaRepository trendReportJpaRepository,
		AgentTraceTrendReportClient reportClient,
		ObjectMapper objectMapper
	) {
		this.repositoryJpaRepository = repositoryJpaRepository;
		this.analysisJpaRepository = analysisJpaRepository;
		this.snapshotJpaRepository = snapshotJpaRepository;
		this.trendReportJpaRepository = trendReportJpaRepository;
		this.reportClient = reportClient;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public TrendReportResponse generateLatestCompletedWeek() {
		LocalDate thisMonday = LocalDate.now(REPORT_ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate start = thisMonday.minusWeeks(1);
		LocalDate end = start.plusDays(6);
		return trendReportJpaRepository.findByPeriodStartAndPeriodEnd(start, end)
			.map(this::toResponse)
			.orElseGet(() -> generate(start, end));
	}

	@Transactional(readOnly = true)
	public TrendReportResponse latest() {
		return trendReportJpaRepository.findFirstByStatusOrderByPeriodEndDesc(TrendReportStatus.PUBLISHED)
			.map(this::toResponse)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No weekly trend report has been generated yet."));
	}

	@Transactional(readOnly = true)
	public List<TrendReportResponse> list(int page, int size) {
		return trendReportJpaRepository.findByStatusOrderByPeriodEndDesc(
			TrendReportStatus.PUBLISHED,
			PageRequest.of(page, Math.min(Math.max(size, 1), 20))
		).stream().map(this::toResponse).toList();
	}

	private TrendReportResponse generate(LocalDate start, LocalDate end) {
		List<AgentRepository> repositories = repositoryJpaRepository
			.findByAgentRelatedTrueAndArchivedFalseAndForkFalse(
				PageRequest.of(0, REPORT_REPOSITORY_LIMIT, Sort.by(Sort.Direction.DESC, "stars"))
			)
			.getContent();
		if (repositories.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No analyzed repositories are available for a report.");
		}

		List<Map<String, Object>> inputs = repositories.stream()
			.map(repository -> reportInput(repository, start, end))
			.toList();
		Map<String, Object> content;
		try {
			content = reportClient.generate(new AgentTraceTrendReportClient.LocalDateRange(start, end), inputs);
		} catch (RuntimeException exception) {
			content = fallbackContent(start, end, inputs);
		}
		content.put("repositories", inputs);
		String title = String.valueOf(content.getOrDefault("title", "Weekly AI Open Source Radar"));
		String modelName = nullableString(content.get("model_name"));
		String promptVersion = nullableString(content.get("prompt_version"));
		TrendReport report = trendReportJpaRepository.save(TrendReport.published(
			start,
			end,
			title,
			objectMapper.writeValueAsString(content),
			modelName,
			promptVersion
		));
		return toResponse(report);
	}

	private Map<String, Object> reportInput(AgentRepository repository, LocalDate start, LocalDate end) {
		RepositoryMetricSnapshot baseline = snapshotJpaRepository
			.findFirstByRepositoryAndCapturedOnLessThanEqualOrderByCapturedOnDesc(repository, start.minusDays(1))
			.orElse(null);
		RepositoryMetricSnapshot latest = snapshotJpaRepository
			.findFirstByRepositoryAndCapturedOnLessThanEqualOrderByCapturedOnDesc(repository, end)
			.orElse(null);
		int startStars = baseline == null ? repository.getStars() : baseline.getStars();
		int endStars = latest == null ? repository.getStars() : latest.getStars();
		int startForks = baseline == null ? repository.getForks() : baseline.getForks();
		int endForks = latest == null ? repository.getForks() : latest.getForks();
		String analysisSummary = analysisJpaRepository.findFirstByRepositoryOrderByRequestedAtDesc(repository)
			.map(analysis -> analysis.getSummary())
			.orElse(repository.getReadmeSummary());

		Map<String, Object> input = new LinkedHashMap<>();
		input.put("repository_id", repository.getId().toString());
		input.put("full_name", repository.getFullName());
		input.put("description", repository.getDescription());
		input.put("language", repository.getLanguage());
		input.put("category", repository.getAgentCategory());
		input.put("stars", endStars);
		input.put("star_delta", endStars - startStars);
		input.put("forks", endForks);
		input.put("fork_delta", endForks - startForks);
		input.put("open_issues", repository.getOpenIssues());
		input.put("pushed_at", repository.getPushedAt() == null ? null : repository.getPushedAt().toString());
		input.put("analysis_summary", analysisSummary);
		return input;
	}

	private Map<String, Object> fallbackContent(LocalDate start, LocalDate end, List<Map<String, Object>> repositories) {
		List<Map<String, Object>> ranked = new ArrayList<>(repositories);
		ranked.sort((left, right) -> Integer.compare(number(right.get("star_delta")), number(left.get("star_delta"))));
		List<Map<String, Object>> featured = ranked.stream().limit(3).map(repository -> Map.<String, Object>of(
			"repository_id", repository.get("repository_id"),
			"reason", "Selected from the highest verified weekly star growth."
		)).toList();
		Map<String, Object> fallback = new LinkedHashMap<>();
		fallback.put("title", "Weekly AI Open Source Radar");
		fallback.put("executive_summary", "This report was generated from verified repository metrics; AI commentary was unavailable.");
		fallback.put("trend_signals", List.of());
		fallback.put("featured_repositories", featured);
		fallback.put("recommendations", List.of("Inspect the featured repositories and their latest analysis before adoption."));
		fallback.put("limitations", List.of("AI narrative generation was unavailable.", "Only stored repository metrics were used."));
		fallback.put("period_start", start.toString());
		fallback.put("period_end", end.toString());
		return fallback;
	}

	private TrendReportResponse toResponse(TrendReport report) {
		Map<String, Object> content = objectMapper.readValue(report.getContentJson(), new TypeReference<>() {});
		return new TrendReportResponse(
			report.getId(),
			report.getPeriodStart(),
			report.getPeriodEnd(),
			report.getTitle(),
			content,
			report.getGeneratedAt()
		);
	}

	private int number(Object value) {
		return value instanceof Number number ? number.intValue() : 0;
	}

	private String nullableString(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
