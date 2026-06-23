package com.yongoh.agenthub_backend.report.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.yongoh.agenthub_backend.global.config.AgentTraceProperties;

@Component
public class AgentTraceTrendReportClient {
	private final RestClient restClient;

	public AgentTraceTrendReportClient(AgentTraceProperties properties) {
		this.restClient = RestClient.builder()
			.requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory())
			.baseUrl(properties.getBaseUrl())
			.build();
	}

	public Map<String, Object> generate(LocalDateRange period, List<Map<String, Object>> repositories) {
		Map<String, Object> request = Map.of(
			"period_start", period.start().toString(),
			"period_end", period.end().toString(),
			"repositories", repositories
		);
		return restClient.post()
			.uri("/v1/trend-reports")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(request)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {});
	}

	public static class LocalDateRange {
		private final java.time.LocalDate start;
		private final java.time.LocalDate end;

		public LocalDateRange(java.time.LocalDate start, java.time.LocalDate end) {
			this.start = start;
			this.end = end;
		}

		public java.time.LocalDate start() {
			return start;
		}

		public java.time.LocalDate end() {
			return end;
		}
	}
}
