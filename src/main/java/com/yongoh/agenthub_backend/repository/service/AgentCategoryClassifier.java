package com.yongoh.agenthub_backend.repository.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class AgentCategoryClassifier {
	public String classify(String readmeMarkdown) {
		String readme = readmeMarkdown == null ? "" : readmeMarkdown.toLowerCase(Locale.ROOT);
		if (contains(readme, "mcp", "model context protocol")) {
			return "mcp";
		}
		if (contains(readme, "multi-agent")) {
			return "multi-agent";
		}
		if (contains(readme, "coding agent", "software engineering agent", "code agent")) {
			return "coding-agent";
		}
		if (contains(readme, "browser agent", "web agent")) {
			return "browser-agent";
		}
		if (contains(readme, "rag", "retrieval augmented generation")) {
			return "rag-agent";
		}
		if (contains(readme, "tool calling", "function calling", "tools")) {
			return "tool-use";
		}
		if (contains(readme, "agent framework", "framework")) {
			return "agent-framework";
		}
		if (contains(readme, "workflow", "planner", "executor")) {
			return "workflow-agent";
		}
		return "unknown-agent";
	}

	private boolean contains(String source, String... keywords) {
		for (String keyword : keywords) {
			if (source.contains(keyword)) {
				return true;
			}
		}
		return false;
	}
}
