package com.yongoh.agenthub_backend.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agent")
public class AgentScoringProperties {
	private int scoreThreshold = 12;
	private List<String> positiveKeywords = List.of(
		"agent", "agents", "ai agent", "llm agent", "agentic", "autonomous agent", "multi-agent",
		"agent framework", "tool calling", "function calling", "planner", "executor", "memory",
		"rag", "workflow", "mcp", "model context protocol", "langchain", "crewai", "autogen",
		"semantic kernel", "browser agent", "coding agent", "machine learning", "deep learning",
		"natural language processing", "computer vision", "model serving", "inference", "transformer",
		"diffusion", "speech recognition"
	);
	private List<String> importantPositiveKeywords = List.of(
		"llm agent", "ai agent", "agentic", "multi-agent", "agent framework", "autonomous agent",
		"model context protocol", "mcp", "llm application", "rag framework", "agent orchestration",
		"model serving", "inference engine"
	);
	private List<String> negativeKeywords = List.of(
		"travel agent", "real estate agent", "user agent", "monitoring agent", "secret agent",
		"ssh agent", "http agent", "browser user agent", "awesome list", "roadmap", "free-for-dev",
		"public api", "public-apis", "system prompt", "prompts collection", "algorithm tutorial"
	);

	public int getScoreThreshold() {
		return scoreThreshold;
	}

	public void setScoreThreshold(int scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
	}

	public List<String> getPositiveKeywords() {
		return positiveKeywords;
	}

	public void setPositiveKeywords(List<String> positiveKeywords) {
		this.positiveKeywords = positiveKeywords;
	}

	public List<String> getImportantPositiveKeywords() {
		return importantPositiveKeywords;
	}

	public void setImportantPositiveKeywords(List<String> importantPositiveKeywords) {
		this.importantPositiveKeywords = importantPositiveKeywords;
	}

	public List<String> getNegativeKeywords() {
		return negativeKeywords;
	}

	public void setNegativeKeywords(List<String> negativeKeywords) {
		this.negativeKeywords = negativeKeywords;
	}
}
