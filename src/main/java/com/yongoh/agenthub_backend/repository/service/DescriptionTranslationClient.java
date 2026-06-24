package com.yongoh.agenthub_backend.repository.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.yongoh.agenthub_backend.global.config.TranslationProperties;

@Component
public class DescriptionTranslationClient {
	private static final Logger log = LoggerFactory.getLogger(DescriptionTranslationClient.class);
	private static final String ENDPOINT = "/chat/completions";

	private final TranslationProperties properties;
	private final RestClient restClient;

	public DescriptionTranslationClient(TranslationProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder()
			.baseUrl(properties.getBaseUrl())
			.build();
	}

	public String translateAboutToKorean(String description) {
		if (!StringUtils.hasText(description)) {
			return null;
		}
		if (!properties.isEnabled() || !StringUtils.hasText(properties.getApiKey()) || isKorean(description)) {
			return description;
		}
		try {
			Map<String, Object> response = restClient.post()
				.uri(ENDPOINT)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(request(description))
				.retrieve()
				.body(new ParameterizedTypeReference<>() {});
			String translated = extractContent(response);
			return StringUtils.hasText(translated) ? translated.trim() : description;
		} catch (RuntimeException exception) {
			log.warn("GitHub about translation failed: error={}", exception.getMessage());
			return description;
		}
	}

	private Map<String, Object> request(String description) {
		return Map.of(
			"model", properties.getModel(),
			"temperature", 0,
			"messages", List.of(
				Map.of(
					"role", "developer",
					"content", "Translate the GitHub repository About/description into natural Korean for Korean developers. Preserve project names, product names, technical terms like LLM/RAG/API, numbers, and emojis. Return only the translated sentence."
				),
				Map.of("role", "user", "content", description)
			)
		);
	}

	@SuppressWarnings("unchecked")
	private String extractContent(Map<String, Object> response) {
		Object choicesValue = response == null ? null : response.get("choices");
		if (!(choicesValue instanceof List<?> choices) || choices.isEmpty()) {
			return null;
		}
		Object firstChoice = choices.get(0);
		if (!(firstChoice instanceof Map<?, ?> choice)) {
			return null;
		}
		Object messageValue = choice.get("message");
		if (!(messageValue instanceof Map<?, ?> message)) {
			return null;
		}
		Object content = message.get("content");
		return content == null ? null : String.valueOf(content);
	}

	private boolean isKorean(String value) {
		long koreanCount = value.chars()
			.filter(character -> character >= 0xAC00 && character <= 0xD7A3)
			.count();
		return koreanCount >= Math.max(4, value.length() / 4);
	}
}
