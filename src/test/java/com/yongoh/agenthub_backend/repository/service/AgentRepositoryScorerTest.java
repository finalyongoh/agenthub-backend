package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.global.config.AgentScoringProperties;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;

class AgentRepositoryScorerTest {
	private final AgentRepositoryScorer scorer = new AgentRepositoryScorer(new AgentScoringProperties());

	@Test
	void scoreAgentRepositoryAboveThreshold() {
		AgentRepository repository = AgentRepository.create(new GithubRepositoryDto(
			1L,
			"owner/agent-framework",
			"owner",
			"agent-framework",
			"AI agent framework",
			"https://github.com/owner/agent-framework",
			null,
			null,
			"main",
			"Java",
			List.of("ai-agent"),
			1200,
			10,
			10,
			1,
			"MIT",
			Instant.now(),
			Instant.now(),
			Instant.now(),
			false,
			false
		));

		String readme = """
			An LLM agent with tool calling, memory, planner, and model context protocol support.
			It includes benchmarks, eval harnesses, pytest integration tests, Docker setup,
			examples, and reproducible success rate results.
			""";
		int score = scorer.score(repository, readme);

		assertThat(score).isGreaterThanOrEqualTo(50);
		assertThat(scorer.isAgentRelated(repository, readme)).isTrue();
	}

	@Test
	void resourceCatalogWithoutStrongAiSignalIsNotAgentRelated() {
		AgentRepository repository = AgentRepository.create(new GithubRepositoryDto(
			2L,
			"sindresorhus/awesome",
			"sindresorhus",
			"awesome",
			"Awesome lists about all kinds of interesting topics",
			"https://github.com/sindresorhus/awesome",
			null,
			null,
			"main",
			null,
			List.of("awesome", "list"),
			477_000,
			30_000,
			100,
			100,
			"CC0-1.0",
			Instant.now(),
			Instant.now(),
			Instant.now(),
			false,
			false
		));

		String readme = "A curated awesome list of resources, guides, tutorials, and public APIs.";

		assertThat(scorer.isAgentRelated(repository, readme)).isFalse();
	}

	@Test
	void resourceCatalogWithStrongLlmSignalCanRemainAgentRelated() {
		AgentRepository repository = AgentRepository.create(new GithubRepositoryDto(
			3L,
			"owner/awesome-llm-agents",
			"owner",
			"awesome-llm-agents",
			"Curated LLM agent frameworks and RAG applications",
			"https://github.com/owner/awesome-llm-agents",
			null,
			null,
			"main",
			"Python",
			List.of("llm", "ai-agent", "rag"),
			2000,
			200,
			20,
			5,
			"MIT",
			Instant.now(),
			Instant.now(),
			Instant.now(),
			false,
			false
		));

		String readme = """
			Awesome LLM agent frameworks, autonomous agents, RAG applications,
			model context protocol integrations, eval examples, and benchmarks.
			""";

		assertThat(scorer.isAgentRelated(repository, readme)).isTrue();
	}
}
