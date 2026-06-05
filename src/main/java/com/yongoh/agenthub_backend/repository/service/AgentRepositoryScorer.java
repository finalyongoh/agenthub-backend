package com.yongoh.agenthub_backend.repository.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
		String target = normalize(repository.getName() + " " + repository.getDescription() + " " + repository.getTopics() + " " + readmeMarkdown);
		int score = 0;
		for (String keyword : properties.getPositiveKeywords()) {
			if (target.contains(normalize(keyword))) {
				score += 2;
			}
		}
		for (String keyword : properties.getImportantPositiveKeywords()) {
			if (target.contains(normalize(keyword))) {
				score += 4;
			}
		}
		for (String keyword : properties.getNegativeKeywords()) {
			if (target.contains(normalize(keyword))) {
				score -= 5;
			}
		}
		if (repository.getStars() > 1000) {
			score += 2;
		} else if (repository.getStars() > 100) {
			score += 1;
		}
		if (repository.getPushedAt() != null && repository.getPushedAt().isAfter(Instant.now().minus(180, ChronoUnit.DAYS))) {
			score += 2;
		}
		if (repository.getTopics() != null && normalize(repository.getTopics()).contains("agent")) {
			score += 3;
		}
		if (repository.isArchived()) {
			score -= 10;
		}
		if (repository.isFork()) {
			score -= 5;
		}
		return score;
	}

	public boolean isAgentRelated(int score) {
		return score >= properties.getScoreThreshold();
	}

	private String normalize(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}
}
