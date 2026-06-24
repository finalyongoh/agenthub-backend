package com.yongoh.agenthub_backend.repository.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

public final class ReadmeTextSanitizer {
	private static final int DEFAULT_MAX_LENGTH = 500;
	private static final int MAX_LINES = 4;

	private ReadmeTextSanitizer() {
	}

	public static String toSummary(String value) {
		return toSummary(value, DEFAULT_MAX_LENGTH);
	}

	public static String toSummary(String value, int maxLength) {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		String decoded = decodeHtmlEntities(value);
		String withoutBlocks = decoded
			.replaceAll("(?is)```.*?```", " ")
			.replaceAll("(?is)<!--.*?-->", " ")
			.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ");
		String withoutMarkdownMedia = withoutBlocks
			.replaceAll("!\\[[^\\]]*]\\([^)]*\\)", " ")
			.replaceAll("\\[([^\\]]+)]\\([^)]*\\)", "$1")
			.replaceAll("(?is)<[^>]+>", " ");

		List<String> lines = new ArrayList<>();
		for (String rawLine : withoutMarkdownMedia.split("\\R+")) {
			String line = cleanLine(rawLine);
			if (!StringUtils.hasText(line) || isNoiseLine(line)) {
				continue;
			}
			lines.add(line);
			if (lines.size() >= MAX_LINES) {
				break;
			}
		}

		String text = String.join(" ", lines)
			.replaceAll("\\s+", " ")
			.trim();
		return truncateAtWord(text, maxLength);
	}

	public static boolean hasText(String value) {
		return StringUtils.hasText(value);
	}

	private static String cleanLine(String rawLine) {
		return rawLine
			.replaceAll("^\\s*#{1,6}\\s*", "")
			.replaceAll("^\\s*>\\s*", "")
			.replaceAll("^\\s*[-*+]\\s+", "")
			.replaceAll("^\\s*\\d+[.)]\\s+", "")
			.replaceAll("[*_`~]", "")
			.replaceAll("\\|", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}

	private static boolean isNoiseLine(String line) {
		String lower = line.toLowerCase();
		int navigationWordCount = 0;
		for (String token : List.of("english", "documentation", "docs", "blog", "paper", "twitter", "slack", "discord", "forum")) {
			if (lower.contains(token)) {
				navigationWordCount++;
			}
		}
		int languageWordCount = 0;
		for (String token : List.of(
			"english", "español", "espanol", "hindi", "deutsch", "français", "francais", "português", "portugues",
			"italiano", "polski", "nederlands", "bahasa", "русский", "中文", "简体中文", "繁體中文", "한국어", "日本語", "العربية", "türkçe"
		)) {
			if (lower.contains(token)) {
				languageWordCount++;
			}
		}
		return line.length() < 3
			|| lower.contains("shields.io")
			|| lower.contains("badge")
			|| lower.contains("github.com")
			|| languageWordCount >= 2
			|| navigationWordCount >= 3
			|| lower.startsWith("http://")
			|| lower.startsWith("https://")
			|| line.matches("^[\\p{Punct}\\s]+$");
	}

	private static String truncateAtWord(String text, int maxLength) {
		if (!StringUtils.hasText(text) || text.length() <= maxLength) {
			return text;
		}
		String suffix = "...";
		int contentLimit = Math.max(0, maxLength - suffix.length());
		int cut = Math.max(text.lastIndexOf(". ", contentLimit), text.lastIndexOf(" ", contentLimit));
		if (cut < contentLimit / 2) {
			cut = contentLimit;
		}
		return text.substring(0, cut).trim() + suffix;
	}

	private static String decodeHtmlEntities(String value) {
		return value
			.replace("&amp;", "&")
			.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&quot;", "\"")
			.replace("&#34;", "\"")
			.replace("&#39;", "'")
			.replace("&apos;", "'")
			.replace("&nbsp;", " ");
	}
}
