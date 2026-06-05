package com.yongoh.agenthub_backend.repository.service;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ReadmeSummaryGenerator {
	private static final int MAX_LENGTH = 300;
	private static final Pattern CODE_BLOCK = Pattern.compile("(?s)```.*?```");
	private static final Pattern IMAGE = Pattern.compile("!\\[[^]]*]\\([^)]*\\)");
	private static final Pattern LINK = Pattern.compile("\\[([^]]+)]\\([^)]*\\)");
	private static final Pattern BADGE = Pattern.compile("\\[[^]]*]\\([^)]*(badge|shields)[^)]*\\)", Pattern.CASE_INSENSITIVE);

	public String generateReadmeSummary(String readmeMarkdown, String repoDescription) {
		if (StringUtils.hasText(repoDescription)) {
			return trim(repoDescription.trim());
		}
		String cleaned = CODE_BLOCK.matcher(readmeMarkdown == null ? "" : readmeMarkdown).replaceAll(" ");
		cleaned = BADGE.matcher(cleaned).replaceAll(" ");
		cleaned = IMAGE.matcher(cleaned).replaceAll(" ");
		cleaned = LINK.matcher(cleaned).replaceAll("$1");
		cleaned = cleaned.replaceAll("(?m)^#{1,6}\\s*", "");
		cleaned = cleaned.replaceAll("(?m)^\\s*[$>`].*$", " ");
		cleaned = cleaned.replaceAll("\\s+", " ").trim();
		String summary = Arrays.stream(cleaned.split("(?<=[.!?。])\\s+"))
			.filter(StringUtils::hasText)
			.limit(3)
			.reduce("", (left, right) -> StringUtils.hasText(left) ? left + " " + right : right);
		return trim(summary);
	}

	private String trim(String value) {
		if (value == null || value.length() <= MAX_LENGTH) {
			return value;
		}
		return value.substring(0, MAX_LENGTH);
	}
}
