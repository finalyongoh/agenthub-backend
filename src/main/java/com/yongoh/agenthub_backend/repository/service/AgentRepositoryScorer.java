package com.yongoh.agenthub_backend.repository.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.yongoh.agenthub_backend.global.config.AgentScoringProperties;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;

@Service
public class AgentRepositoryScorer {
	private final AgentScoringProperties properties;

	public AgentRepositoryScorer(AgentScoringProperties properties) {
		this.properties = properties;
	}

	public int score(AgentRepository repository, String readmeMarkdown) {
		String target = targetText(repository, readmeMarkdown);

		int score = 0;
		score += capabilityDepthScore(target);
		score += evaluationEvidenceScore(target);
		score += reproducibilityScore(target);
		score += operationalQualityScore(target);
		score += adoptionScore(repository);
		score -= riskPenalty(repository, target);

		return clamp(score, 0, 100);
	}

	public boolean isAgentRelated(AgentRepository repository, String readmeMarkdown) {
		String target = targetText(repository, readmeMarkdown);
		if (isGeneralResourceCatalog(target) && !hasStrongAiMlAgentSignal(target)) {
			return false;
		}
		int relevanceScore = 0;
		for (String keyword : properties.getPositiveKeywords()) {
			if (target.contains(normalize(keyword))) {
				relevanceScore += 2;
			}
		}
		for (String keyword : properties.getImportantPositiveKeywords()) {
			if (target.contains(normalize(keyword))) {
				relevanceScore += 4;
			}
		}
		for (String keyword : properties.getNegativeKeywords()) {
			if (target.contains(normalize(keyword))) {
				relevanceScore -= 5;
			}
		}
		if (repository.getTopics() != null && normalize(repository.getTopics()).contains("agent")) {
			relevanceScore += 3;
		}
		if (repository.isArchived()) {
			relevanceScore -= 10;
		}
		if (repository.isFork()) {
			relevanceScore -= 5;
		}
		return relevanceScore >= properties.getScoreThreshold() && hasStrongAiMlAgentSignal(target);
	}

	public boolean isAgentRelated(int score) {
		return score >= 35;
	}

	private int capabilityDepthScore(String target) {
		int score = 0;
		score += containsAny(target, "agent loop", "planner", "executor", "orchestrator", "workflow engine") ? 6 : 0;
		score += containsAny(target, "tool calling", "function calling", "tool registry", "tools", "mcp", "model context protocol") ? 5 : 0;
		score += containsAny(target, "memory", "state", "scratchpad", "trajectory", "reflection") ? 4 : 0;
		score += containsAny(target, "multi-agent", "multi agent", "swarm", "crew", "autogen", "crewai") ? 4 : 0;
		score += containsAny(target, "browser agent", "coding agent", "web agent", "rag agent", "llm agent", "ai agent") ? 4 : 0;
		score += containsAny(target, "guardrail", "policy", "sandbox", "permission") ? 2 : 0;
		return Math.min(score, 25);
	}

	private int evaluationEvidenceScore(String target) {
		int score = 0;
		score += containsAny(target, "benchmark", "benchmarks", "eval", "evaluation", "harness") ? 8 : 0;
		score += containsAny(target, "swe-bench", "swebench", "webarena", "agentbench", "toolbench", "tau-bench", "τ-bench", "gaia") ? 7 : 0;
		score += containsAny(target, "success rate", "solve rate", "pass rate", "accuracy", "win rate", "score", "leaderboard") ? 6 : 0;
		score += containsAny(target, "results.json", "result.json", "results/", "experiments/", "metrics") ? 5 : 0;
		score += containsAny(target, "pytest", "vitest", "jest", "unittest", "integration test", "e2e") ? 4 : 0;
		return Math.min(score, 30);
	}

	private int reproducibilityScore(String target) {
		int score = 0;
		score += containsAny(target, "quickstart", "installation", "getting started", "usage") ? 3 : 0;
		score += containsAny(target, "dockerfile", "docker compose", "docker-compose", "devcontainer") ? 4 : 0;
		score += containsAny(target, "requirements.txt", "pyproject.toml", "package.json", "pnpm-lock", "uv.lock") ? 3 : 0;
		score += containsAny(target, "run benchmark", "run eval", "make benchmark", "make eval", "npm test", "pytest") ? 5 : 0;
		score += containsAny(target, "example", "examples/", "sample", "demo") ? 3 : 0;
		score += containsAny(target, "seed", "config", "yaml", "toml") ? 2 : 0;
		return Math.min(score, 20);
	}

	private int operationalQualityScore(String target) {
		int score = 0;
		score += containsAny(target, "timeout", "retry", "rate limit", "backoff") ? 3 : 0;
		score += containsAny(target, "logging", "tracing", "observability", "telemetry", "monitoring") ? 3 : 0;
		score += containsAny(target, "sandbox", "permission", "policy", "isolation") ? 3 : 0;
		score += containsAny(target, "secret", "environment variable", ".env", "configuration") ? 2 : 0;
		score += containsAny(target, "test", "tests", "ci", "github actions") ? 2 : 0;
		score += containsAny(target, "error handling", "fallback") ? 2 : 0;
		return Math.min(score, 15);
	}

	private int adoptionScore(AgentRepository repository) {
		int score = 0;
		score += Math.min(4, logBucket(repository.getStars(), 10, 100, 1_000, 10_000));
		score += Math.min(2, logBucket(repository.getForks(), 5, 50, 500, 5_000));
		score += freshnessPoints(repository.getPushedAt(), 2);
		score += repository.getOpenIssues() > 0 ? 1 : 0;
		score += repository.getWatchers() > 0 ? 1 : 0;
		return Math.min(score, 10);
	}

	private int riskPenalty(AgentRepository repository, String target) {
		int penalty = 0;
		if (repository.isArchived()) {
			penalty += 20;
		}
		if (repository.isFork()) {
			penalty += 8;
		}
		if (repository.getPushedAt() == null || repository.getPushedAt().isBefore(Instant.now().minus(365, ChronoUnit.DAYS))) {
			penalty += 8;
		}
		if (containsAny(target, "production-ready", "secure by default", "fully autonomous", "guaranteed", "perfect", "100%")) {
			penalty += 5;
		}
		if (containsAny(target, "benchmark", "leaderboard", "sota", "state-of-the-art")
			&& !containsAny(target, "results", "eval", "evaluation", "reproduce", "docker", "pytest", "ci")) {
			penalty += 8;
		}
		if (!containsAny(target, "test", "tests", "eval", "benchmark", "example", "docker", "quickstart")) {
			penalty += 5;
		}
		if (isGeneralResourceCatalog(target) && !hasStrongAiMlAgentSignal(target)) {
			penalty += 12;
		}
		return Math.min(penalty, 30);
	}

	private boolean hasStrongAiMlAgentSignal(String target) {
		return containsAny(
			target,
			"ai-agent",
			"ai agent",
			"llm agent",
			"agentic",
			"autonomous agent",
			"multi-agent",
			"agent framework",
			"agent orchestration",
			"tool calling",
			"function calling",
			"rag",
			"retrieval augmented generation",
			"model context protocol",
			"mcp",
			"llm application",
			"large language model",
			"machine learning",
			"deep learning",
			"natural language processing",
			"computer vision",
			"model serving",
			"inference engine",
			"transformer",
			"diffusion"
		);
	}

	private boolean isGeneralResourceCatalog(String target) {
		return containsAny(
			target,
			"awesome",
			"roadmap",
			"free-for-dev",
			"free for dev",
			"public api",
			"public-apis",
			"system prompt",
			"prompts",
			"guide",
			"tutorial",
			"interview",
			"algorithm"
		);
	}

	private String targetText(AgentRepository repository, String readmeMarkdown) {
		return normalize(
			repository.getName()
				+ " "
				+ repository.getFullName()
				+ " "
				+ repository.getDescription()
				+ " "
				+ repository.getTopics()
				+ " "
				+ repository.getLanguage()
				+ " "
				+ readmeMarkdown
		);
	}

	private boolean containsAny(String target, String... needles) {
		return containsAny(target, List.of(needles));
	}

	private boolean containsAny(String target, List<String> needles) {
		for (String needle : needles) {
			if (target.contains(normalize(needle))) {
				return true;
			}
		}
		return false;
	}

	private int logBucket(int value, int first, int second, int third, int fourth) {
		if (value >= fourth) {
			return 4;
		}
		if (value >= third) {
			return 3;
		}
		if (value >= second) {
			return 2;
		}
		if (value >= first) {
			return 1;
		}
		return 0;
	}

	private int freshnessPoints(Instant pushedAt, int max) {
		if (pushedAt == null) {
			return 0;
		}
		Instant now = Instant.now();
		if (pushedAt.isAfter(now.minus(30, ChronoUnit.DAYS))) {
			return max;
		}
		if (pushedAt.isAfter(now.minus(180, ChronoUnit.DAYS))) {
			return Math.max(1, max - 1);
		}
		return 0;
	}

	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private String normalize(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}
}
