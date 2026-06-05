package com.yongoh.agenthub_backend.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yongoh.agenthub_backend.global.config.GithubProperties;

@Component
public class GithubReadmeSyncScheduler {
	private final GithubSyncJobLauncher jobLauncher;
	private final GithubProperties properties;

	public GithubReadmeSyncScheduler(GithubSyncJobLauncher jobLauncher, GithubProperties properties) {
		this.jobLauncher = jobLauncher;
		this.properties = properties;
	}

	@Scheduled(cron = "${github.sync.cron:0 0 3 * * *}")
	public void run() {
		if (properties.getSync().isEnabled()) {
			jobLauncher.launch(properties.getSync().getMaxRepositoriesPerRun(), false);
		}
	}
}
