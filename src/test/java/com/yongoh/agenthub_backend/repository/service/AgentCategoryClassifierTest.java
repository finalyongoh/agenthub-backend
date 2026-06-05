package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AgentCategoryClassifierTest {
	private final AgentCategoryClassifier classifier = new AgentCategoryClassifier();

	@Test
	void classifyByPriority() {
		assertThat(classifier.classify("This is an MCP multi-agent framework.")).isEqualTo("mcp");
		assertThat(classifier.classify("A software engineering agent for code tasks.")).isEqualTo("coding-agent");
		assertThat(classifier.classify("Planner and executor workflow.")).isEqualTo("workflow-agent");
	}
}
