package com.yongoh.agenthub_backend.report.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class TrendReportService {
	private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Seoul");
	private static final int REPORT_REPOSITORY_LIMIT = 20;

	private final AgentRepositoryJpaRepository repositoryJpaRepository;
	private final RepositoryAnalysisRepository analysisRepository;
	private final RepositoryMetricSnapshotJpaRepository snapshotJpaRepository;
	private final TrendReportJpaRepository trendReportJpaRepository;
	private final AgentTraceTrendReportClient reportClient;
	private final ObjectMapper objectMapper;

	public TrendReportService(
		AgentRepositoryJpaRepository repositoryJpaRepository,
		RepositoryAnalysisRepository analysisRepository,
		RepositoryMetricSnapshotJpaRepository snapshotJpaRepository,
		TrendReportJpaRepository trendReportJpaRepository,
		AgentTraceTrendReportClient reportClient,
		ObjectMapper objectMapper
	) {
		this.repositoryJpaRepository = repositoryJpaRepository;
		this.analysisRepository = analysisRepository;
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

	@Transactional
	public TrendReportResponse generatePeriod(LocalDate start, LocalDate end) {
		LocalDate thisMonday = LocalDate.now(REPORT_ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		if (start.getDayOfWeek() != DayOfWeek.MONDAY || end.getDayOfWeek() != DayOfWeek.SUNDAY) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A report period must run from Monday through Sunday.");
		}
		if (!end.equals(start.plusDays(6)) || !end.isBefore(thisMonday)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only a completed seven-day report period can be generated.");
		}
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
		content = normalizeReportContent(start, end, content, inputs);
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
		String analysisSummary = analysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repository.getId())
			.map(analysis -> {
				try {
					if (analysis.getResultJson() != null) {
						Map<String, Object> result = objectMapper.readValue(analysis.getResultJson(), new TypeReference<Map<String, Object>>() {});
						if (result.get("summary") != null) {
							return String.valueOf(result.get("summary"));
						}
					}
				} catch (Exception e) {
					// fallback
				}
				return repository.getReadmeSummary();
			})
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
		input.put("agent_score", repository.getAgentScore());
		input.put("analysis_summary", analysisSummary);
		return input;
	}

	private Map<String, Object> normalizeReportContent(
		LocalDate start,
		LocalDate end,
		Map<String, Object> content,
		List<Map<String, Object>> repositories
	) {
		Map<String, Object> normalized = new LinkedHashMap<>(content == null ? Map.of() : content);
		normalized.putIfAbsent("title", "주간 AI 오픈소스 리포트");
		normalized.putIfAbsent("executive_summary", defaultExecutiveSummary(repositories));
		normalized.put("repositories", repositories);
		normalized.put("trend_signals", normalizedTrendSignals(repositories));
		normalized.put("featured_repositories", normalizedFeaturedRepositories(normalized.get("featured_repositories"), repositories));
		normalized.putIfAbsent("recommendations", defaultRecommendations(repositories));
		normalized.put("limitations", normalizedLimitations(normalized.get("limitations"), repositories, start, end));
		normalized.put("period_start", start.toString());
		normalized.put("period_end", end.toString());
		return normalized;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> normalizedFeaturedRepositories(Object featuredValue, List<Map<String, Object>> repositories) {
		Map<String, Map<String, Object>> repositoryById = new LinkedHashMap<>();
		for (Map<String, Object> repository : repositories) {
			repositoryById.put(text(repository.get("repository_id"), ""), repository);
		}

		List<Map<String, Object>> featured = new ArrayList<>();
		if (featuredValue instanceof List<?> rawFeatured) {
			for (Object item : rawFeatured) {
				if (!(item instanceof Map<?, ?> rawItem)) {
					continue;
				}
				String repositoryId = text(((Map<String, Object>) rawItem).get("repository_id"), "");
				Map<String, Object> repository = repositoryById.get(repositoryId);
				if (repository == null || containsRepositoryId(featured, repositoryId)) {
					continue;
				}
				Map<String, Object> normalized = new LinkedHashMap<>();
				normalized.put("repository_id", repositoryId);
				normalized.put("reason", text(((Map<String, Object>) rawItem).get("reason"), featuredReason(repository)));
				featured.add(normalized);
			}
		}

		List<Map<String, Object>> ranked = new ArrayList<>(repositories);
		ranked.sort((left, right) -> Integer.compare(featureScore(right), featureScore(left)));
		for (Map<String, Object> repository : ranked) {
			if (featured.size() >= 3) {
				break;
			}
			String repositoryId = text(repository.get("repository_id"), "");
			if (containsRepositoryId(featured, repositoryId)) {
				continue;
			}
			Map<String, Object> normalized = new LinkedHashMap<>();
			normalized.put("repository_id", repositoryId);
			normalized.put("reason", featuredReason(repository));
			featured.add(normalized);
		}
		return featured;
	}

	private boolean containsRepositoryId(List<Map<String, Object>> featured, String repositoryId) {
		return featured.stream().anyMatch(item -> repositoryId.equals(text(item.get("repository_id"), "")));
	}

	private List<Map<String, Object>> normalizedTrendSignals(List<Map<String, Object>> repositories) {
		return List.of(
			trendSignal("수집 레포 수", repositories.size(), "이번 리포트는 수집된 AI/agent 후보 레포 " + repositories.size() + "개를 기준으로 구성했습니다."),
			trendSignal("평균 Agent Score", average(repositories, "agent_score"), "수집 레포의 평균 Agent Score입니다."),
			trendSignal("주요 언어", mostFrequent(repositories, "language", "언어 미확인"), "수집 목록에서 가장 많이 관찰된 구현 언어입니다."),
			trendSignal("주간 스타 변화", sum(repositories, "star_delta"), "스냅샷 기준 주간 스타 변화 합계입니다.")
		);
	}

	private List<String> defaultRecommendations(List<Map<String, Object>> repositories) {
		String primaryLanguage = mostFrequent(repositories, "language", "주요 언어");
		return List.of(
			"Agent Score가 높고 최근 업데이트가 있는 레포부터 상세 분석을 확인하세요.",
			primaryLanguage + " 생태계에서 반복적으로 등장하는 구현 방식과 의존성을 비교해보세요.",
			"스냅샷 데이터가 쌓인 뒤에는 주간 성장률과 실제 사용 가능성을 함께 보세요."
		);
	}

	@SuppressWarnings("unchecked")
	private List<String> normalizedLimitations(Object limitationsValue, List<Map<String, Object>> repositories, LocalDate start, LocalDate end) {
		List<String> limitations = new ArrayList<>();
		if (limitationsValue instanceof List<?> rawLimitations) {
			for (Object limitation : rawLimitations) {
				if (hasText(limitation)) {
					limitations.add(String.valueOf(limitation));
				}
			}
		}
		boolean hasWeeklyDelta = repositories.stream()
			.anyMatch(repository -> number(repository.get("star_delta")) != 0 || number(repository.get("fork_delta")) != 0);
		if (!hasWeeklyDelta) {
			String snapshotLimitation = start + "부터 " + end + "까지의 비교 스냅샷이 부족해 주간 증감은 0으로 표시합니다.";
			if (!limitations.contains(snapshotLimitation)) {
				limitations.add(snapshotLimitation);
			}
		}
		return limitations;
	}

	private String defaultExecutiveSummary(List<Map<String, Object>> repositories) {
		int repositoryCount = repositories.size();
		String primaryLanguage = mostFrequent(repositories, "language", "주요 언어");
		String primaryTopic = mostFrequent(repositories, "category", "AI agent");
		return "이번 주 수집된 " + repositoryCount + "개 AI/agent 레포를 보면 "
			+ primaryTopic + " 흐름과 " + primaryLanguage + " 기반 구현이 두드러집니다.";
	}

	private Map<String, Object> fallbackContent(LocalDate start, LocalDate end, List<Map<String, Object>> repositories) {
		List<Map<String, Object>> ranked = new ArrayList<>(repositories);
		ranked.sort((left, right) -> Integer.compare(featureScore(right), featureScore(left)));
		List<Map<String, Object>> featured = ranked.stream().limit(3).map(repository -> {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("repository_id", repository.get("repository_id"));
			item.put("reason", featuredReason(repository));
			return item;
		}).toList();
		int repositoryCount = repositories.size();
		int koreanSummaryCount = (int) repositories.stream()
			.filter(repository -> hasText(repository.get("analysis_summary")))
			.count();
		int averageScore = average(repositories, "agent_score");
		int totalStarDelta = sum(repositories, "star_delta");
		String primaryLanguage = mostFrequent(repositories, "language", "주요 언어 미확인");
		String primaryTopic = mostFrequent(repositories, "category", "AI agent");
		String topRepository = ranked.isEmpty() ? "주목 레포" : text(ranked.get(0).get("full_name"), "주목 레포");

		Map<String, Object> fallback = new LinkedHashMap<>();
		fallback.put("title", "주간 AI 오픈소스 리포트");
		fallback.put(
			"executive_summary",
			"이번 주 수집된 " + repositoryCount + "개 AI/agent 레포를 보면 "
				+ primaryTopic + " 흐름이 두드러지고, " + topRepository + " 같은 레포가 주요 신호로 잡혔습니다."
		);
		fallback.put("trend_signals", List.of(
			trendSignal("수집 레포 수", repositoryCount, "이번 리포트는 수집된 AI/agent 후보 레포 " + repositoryCount + "개를 기준으로 구성했습니다."),
			trendSignal("평균 Agent Score", averageScore, "평균 Agent Score는 " + averageScore + "점이며, 구현 근거와 운영 품질 신호를 함께 반영했습니다."),
			trendSignal("주요 언어", primaryLanguage, primaryLanguage + " 기반 레포가 이번 수집 목록에서 가장 많이 관찰됐습니다."),
			trendSignal("주간 스타 변화", totalStarDelta, "수집된 레포의 주간 스타 변화 합계는 " + totalStarDelta + "입니다.")
		));
		fallback.put("featured_repositories", featured);
		fallback.put("recommendations", List.of(
			"Agent Score가 높고 최근 업데이트가 있는 레포부터 상세 분석을 확인하세요.",
			primaryLanguage + " 생태계에서 반복적으로 등장하는 토픽과 구현 방식을 비교해보세요."
		));
		fallback.put("limitations", List.of(
			"히스토리 데이터가 충분히 쌓이기 전까지 주간 성장률은 스타 변화와 최신 업데이트 신호를 함께 참고합니다.",
			"레포별 실제 성능은 벤치마크와 실행 환경에서 추가 확인이 필요합니다."
		));
		fallback.put("korean_summary_count", koreanSummaryCount);
		fallback.put("period_start", start.toString());
		fallback.put("period_end", end.toString());
		return fallback;
	}

	private TrendReportResponse toResponse(TrendReport report) {
		Map<String, Object> content = objectMapper.readValue(report.getContentJson(), new TypeReference<>() {});
		refreshRepositorySignals(content);
		return new TrendReportResponse(
			report.getId(),
			report.getPeriodStart(),
			report.getPeriodEnd(),
			report.getTitle(),
			content,
			report.getGeneratedAt()
		);
	}

	@SuppressWarnings("unchecked")
	private void refreshRepositorySignals(Map<String, Object> content) {
		Object repositoriesValue = content.get("repositories");
		if (!(repositoriesValue instanceof List<?> repositories)) {
			return;
		}
		List<UUID> ids = repositories.stream()
			.filter(Map.class::isInstance)
			.map(repository -> ((Map<String, Object>) repository).get("repository_id"))
			.map(this::uuidOrNull)
			.filter(id -> id != null)
			.toList();
		if (ids.isEmpty()) {
			return;
		}
		Map<String, AgentRepository> currentById = new LinkedHashMap<>();
		repositoryJpaRepository.findAllById(ids)
			.forEach(repository -> currentById.put(repository.getId().toString(), repository));

		Map<String, Map<String, Object>> refreshedById = new LinkedHashMap<>();
		for (Object repositoryValue : repositories) {
			if (!(repositoryValue instanceof Map<?, ?> rawRepository)) {
				continue;
			}
			Map<String, Object> repository = (Map<String, Object>) rawRepository;
			String repositoryId = text(repository.get("repository_id"), "");
			AgentRepository current = currentById.get(repositoryId);
			if (current == null) {
				continue;
			}
			repository.put("full_name", current.getFullName());
			repository.put("description", current.getDescription());
			repository.put("language", current.getLanguage());
			repository.put("category", current.getAgentCategory());
			repository.put("stars", current.getStars());
			repository.put("forks", current.getForks());
			repository.put("open_issues", current.getOpenIssues());
			repository.put("pushed_at", current.getPushedAt() == null ? null : current.getPushedAt().toString());
			repository.put("agent_score", current.getAgentScore());
			refreshedById.put(repositoryId, repository);
		}

		Object featuredValue = content.get("featured_repositories");
		if (!(featuredValue instanceof List<?> featuredRepositories)) {
			return;
		}
		for (Object featuredValueItem : featuredRepositories) {
			if (!(featuredValueItem instanceof Map<?, ?> rawFeatured)) {
				continue;
			}
			Map<String, Object> featured = (Map<String, Object>) rawFeatured;
			String repositoryId = text(featured.get("repository_id"), "");
			Map<String, Object> repository = refreshedById.get(repositoryId);
			if (repository != null) {
				featured.put("reason", featuredReason(repository));
			}
		}
	}

	private UUID uuidOrNull(Object value) {
		try {
			return value == null ? null : UUID.fromString(String.valueOf(value));
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private int number(Object value) {
		return value instanceof Number number ? number.intValue() : 0;
	}

	private String nullableString(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private Map<String, Object> trendSignal(String label, Object value, String narrative) {
		Map<String, Object> signal = new LinkedHashMap<>();
		signal.put("label", label);
		signal.put("value", value);
		signal.put("narrative", narrative);
		return signal;
	}

	private int featureScore(Map<String, Object> repository) {
		return number(repository.get("star_delta")) * 3
			+ number(repository.get("agent_score")) * 2
			+ Math.min(number(repository.get("stars")) / 1_000, 50);
	}

	private String featuredReason(Map<String, Object> repository) {
		String name = text(repository.get("full_name"), "이 레포");
		String category = text(repository.get("category"), "AI agent");
		return name + "는 " + category + " 흐름에서 Agent Score "
			+ number(repository.get("agent_score")) + "점, Stars "
			+ String.format("%,d", number(repository.get("stars"))) + " 기준으로 주목할 만합니다.";
	}

	private int average(List<Map<String, Object>> repositories, String key) {
		if (repositories.isEmpty()) {
			return 0;
		}
		return Math.round((float) sum(repositories, key) / repositories.size());
	}

	private int sum(List<Map<String, Object>> repositories, String key) {
		int total = 0;
		for (Map<String, Object> repository : repositories) {
			total += number(repository.get(key));
		}
		return total;
	}

	private String mostFrequent(List<Map<String, Object>> repositories, String key, String fallback) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (Map<String, Object> repository : repositories) {
			String value = text(repository.get(key), "");
			if (value.isBlank()) {
				continue;
			}
			counts.put(value, counts.getOrDefault(value, 0) + 1);
		}
		String selected = fallback;
		int selectedCount = 0;
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			if (entry.getValue() > selectedCount) {
				selected = entry.getKey();
				selectedCount = entry.getValue();
			}
		}
		return selected;
	}

	private boolean hasText(Object value) {
		return value != null && !String.valueOf(value).isBlank();
	}

	private String text(Object value, String fallback) {
		return hasText(value) ? String.valueOf(value) : fallback;
	}
}
