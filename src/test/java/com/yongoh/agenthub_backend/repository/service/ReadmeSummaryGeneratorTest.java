package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReadmeSummaryGeneratorTest {
	private final ReadmeSummaryGenerator generator = new ReadmeSummaryGenerator();

	@Test
	void useDescriptionFirst() {
		assertThat(generator.generateReadmeSummary("# Title\nREADME body", "Repository description"))
			.isEqualTo("Repository description");
	}

	@Test
	void cleanupMarkdownForSummary() {
		String summary = generator.generateReadmeSummary("""
			![badge](https://img.shields.io/badge/test)
			# Agent Framework
			```bash
			npm install
			```
			[AgentHub](https://example.com) helps build LLM agents. It supports tool calling.
			""", null);

		assertThat(summary).contains("Agent Framework");
		assertThat(summary).contains("AgentHub helps build LLM agents");
		assertThat(summary).doesNotContain("npm install");
	}
}
