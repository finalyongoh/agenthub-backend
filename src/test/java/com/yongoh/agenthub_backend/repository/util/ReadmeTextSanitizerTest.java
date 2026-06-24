package com.yongoh.agenthub_backend.repository.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReadmeTextSanitizerTest {
	@Test
	void toSummaryRemovesHtmlBadgesAndKeepsReadableText() {
		String readme = """
			<a href="https://example.com"><img src="https://img.shields.io/badge/build-passing"></a>
			<a href="https://github.com/example/project">GitHub</a>

			# CrewAI

			CrewAI is a framework for orchestrating role-playing autonomous AI agents.
			It helps teams build multi-agent workflows with tools and tasks.
			""";

		String summary = ReadmeTextSanitizer.toSummary(readme);

		assertThat(summary).doesNotContain("<a href");
		assertThat(summary).doesNotContain("shields.io");
		assertThat(summary).contains("CrewAI");
		assertThat(summary).contains("framework for orchestrating");
	}

	@Test
	void toSummaryDecodesEscapedHtmlBeforeCleaning() {
		String readme = "&lt;a href=&quot;https://example.com&quot;&gt;badge&lt;/a&gt;\n\nAgent runtime for AI workflows.";

		String summary = ReadmeTextSanitizer.toSummary(readme);

		assertThat(summary).doesNotContain("&lt;");
		assertThat(summary).doesNotContain("<a");
		assertThat(summary).contains("Agent runtime for AI workflows");
	}

	@Test
	void toSummaryKeepsEllipsisInsideMaximumLength() {
		String summary = ReadmeTextSanitizer.toSummary("Agent ".repeat(200), 20);

		assertThat(summary).hasSizeLessThanOrEqualTo(20);
		assertThat(summary).endsWith("...");
	}

	@Test
	void toSummarySkipsReadmeNavigationLines() {
		String readme = """
			English 简体中文 繁體中文 한국어
			English Español हिन्दी Русский
			Documentation Blog Paper Twitter Slack

			Transformers provides APIs and tools to easily download and train state-of-the-art pretrained models.
			""";

		String summary = ReadmeTextSanitizer.toSummary(readme);

		assertThat(summary).doesNotContain("English");
		assertThat(summary).doesNotContain("Español");
		assertThat(summary).doesNotContain("Documentation Blog");
		assertThat(summary).contains("Transformers provides APIs");
	}
}
