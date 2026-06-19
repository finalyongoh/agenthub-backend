package com.yongoh.agenthub_backend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agenttrace.summary")
public class AgentTraceProperties {
	private String baseUrl = "http://localhost:8000";

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
