package com.yongoh.agenthub_backend.repository.controller;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yongoh.agenthub_backend.batch.GithubReadmeSyncJobConfig;
import com.yongoh.agenthub_backend.batch.GithubSyncJobLauncher;
import com.yongoh.agenthub_backend.global.config.GithubProperties;
import com.yongoh.agenthub_backend.repository.dto.BatchSyncLogDto;
import com.yongoh.agenthub_backend.repository.dto.BatchSyncLogListResponse;
import com.yongoh.agenthub_backend.repository.dto.GithubSyncRequest;
import com.yongoh.agenthub_backend.repository.dto.GithubSyncResponse;
import com.yongoh.agenthub_backend.repository.model.BatchSyncLogStatus;
import com.yongoh.agenthub_backend.repository.repository.BatchSyncLogJpaRepository;

@RestController
public class AdminGithubSyncController {
	private final GithubSyncJobLauncher jobLauncher;
	private final GithubProperties properties;
	private final BatchSyncLogJpaRepository syncLogRepository;

	public AdminGithubSyncController(
		GithubSyncJobLauncher jobLauncher,
		GithubProperties properties,
		BatchSyncLogJpaRepository syncLogRepository
	) {
		this.jobLauncher = jobLauncher;
		this.properties = properties;
		this.syncLogRepository = syncLogRepository;
	}

	@PostMapping("/api/admin/github/sync-agent-repositories")
	public GithubSyncResponse syncAgentRepositories(@RequestBody(required = false) GithubSyncRequest request) {
		int limit = request == null || request.getLimit() == null
			? properties.getSync().getMaxRepositoriesPerRun()
			: request.getLimit();
		boolean force = request != null && request.isForce();
		JobExecution execution = jobLauncher.launch(limit, force);
		return new GithubSyncResponse(
			GithubReadmeSyncJobConfig.JOB_NAME,
			execution.getId(),
			execution.getStatus().name().toLowerCase()
		);
	}

	@GetMapping("/api/admin/github/sync-logs")
	public BatchSyncLogListResponse findSyncLogs(
		@RequestParam(required = false) String status,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "20") int limit
	) {
		PageRequest pageRequest = PageRequest.of(
			Math.max(page - 1, 0),
			Math.max(limit, 1),
			Sort.by(Sort.Direction.DESC, "startedAt")
		);
		var logs = status == null || status.isBlank()
			? syncLogRepository.findAll(pageRequest)
			: syncLogRepository.findByStatus(BatchSyncLogStatus.valueOf(status.toUpperCase()), pageRequest);
		return new BatchSyncLogListResponse(
			logs.stream().map(BatchSyncLogDto::from).toList(),
			page,
			limit,
			logs.getTotalElements()
		);
	}
}
