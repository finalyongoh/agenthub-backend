package com.yongoh.agenthub_backend.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "github")
public class GithubProperties {
	private Api api = new Api();
	private Sync sync = new Sync();

	public Api getApi() {
		return api;
	}

	public void setApi(Api api) {
		this.api = api;
	}

	public Sync getSync() {
		return sync;
	}

	public void setSync(Sync sync) {
		this.sync = sync;
	}

	public static class Api {
		private String baseUrl = "https://api.github.com";
		private String token = "";
		private String version = "2022-11-28";

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}
	}

	public static class Sync {
		private boolean enabled = true;
		private String cron = "0 0 3 * * *";
		private int maxRepositoriesPerRun = 100;
		private int readmeMaxLength = 100000;
		private String searchSort = "stars";
		private String searchOrder = "desc";
		private int minStars = 20;
		private int retryCount = 2;
		private int fileTreeMaxPaths = 200;
		private List<String> queries = List.of(
			"\"ai agent\" in:readme stars:>20",
			"\"llm agent\" in:readme stars:>20",
			"\"multi-agent\" in:readme stars:>20",
			"\"agent framework\" in:readme stars:>20",
			"\"model context protocol\" in:readme stars:>20",
			"\"coding agent\" in:readme stars:>20"
		);

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getCron() {
			return cron;
		}

		public void setCron(String cron) {
			this.cron = cron;
		}

		public int getMaxRepositoriesPerRun() {
			return maxRepositoriesPerRun;
		}

		public void setMaxRepositoriesPerRun(int maxRepositoriesPerRun) {
			this.maxRepositoriesPerRun = maxRepositoriesPerRun;
		}

		public int getReadmeMaxLength() {
			return readmeMaxLength;
		}

		public void setReadmeMaxLength(int readmeMaxLength) {
			this.readmeMaxLength = readmeMaxLength;
		}

		public String getSearchSort() {
			return searchSort;
		}

		public void setSearchSort(String searchSort) {
			this.searchSort = searchSort;
		}

		public String getSearchOrder() {
			return searchOrder;
		}

		public void setSearchOrder(String searchOrder) {
			this.searchOrder = searchOrder;
		}

		public int getMinStars() {
			return minStars;
		}

		public void setMinStars(int minStars) {
			this.minStars = minStars;
		}

		public int getRetryCount() {
			return retryCount;
		}

		public void setRetryCount(int retryCount) {
			this.retryCount = retryCount;
		}

		public int getFileTreeMaxPaths() {
			return fileTreeMaxPaths;
		}

		public void setFileTreeMaxPaths(int fileTreeMaxPaths) {
			this.fileTreeMaxPaths = fileTreeMaxPaths;
		}

		public List<String> getQueries() {
			return queries;
		}

		public void setQueries(List<String> queries) {
			this.queries = queries;
		}
	}
}
