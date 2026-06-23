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
}
