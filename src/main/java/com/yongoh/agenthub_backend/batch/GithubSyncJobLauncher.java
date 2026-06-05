package com.yongoh.agenthub_backend.batch;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.repository.model.BatchSyncLog;
import com.yongoh.agenthub_backend.repository.repository.BatchSyncLogJpaRepository;
import com.yongoh.agenthub_backend.repository.service.SyncStatistics;

@Service
public class GithubSyncJobLauncher {
	private static final Logger log = LoggerFactory.getLogger(GithubSyncJobLauncher.class);

	private final JobLauncher jobLauncher;
	private final Job githubReadmeSyncJob;
	private final JobExplorer jobExplorer;
	private final BatchSyncLogJpaRepository syncLogRepository;
	private final AtomicBoolean running = new AtomicBoolean(false);

	public GithubSyncJobLauncher(
		JobLauncher jobLauncher,
		Job githubReadmeSyncJob,
		JobExplorer jobExplorer,
		BatchSyncLogJpaRepository syncLogRepository
	) {
		this.jobLauncher = jobLauncher;
		this.githubReadmeSyncJob = githubReadmeSyncJob;
		this.jobExplorer = jobExplorer;
		this.syncLogRepository = syncLogRepository;
	}

	public JobExecution launch(int limit, boolean force) {
		Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions(GithubReadmeSyncJobConfig.JOB_NAME);
		if (!running.compareAndSet(false, true) || !runningExecutions.isEmpty()) {
			throw new ApiException(HttpStatus.CONFLICT, "GITHUB_SYNC_409", "이미 githubReadmeSyncJob이 실행 중입니다.");
		}
		BatchSyncLog syncLog = syncLogRepository.save(BatchSyncLog.started(GithubReadmeSyncJobConfig.JOB_NAME));
		log.info("githubReadmeSyncJob started");
		try {
			JobExecution execution = jobLauncher.run(githubReadmeSyncJob, new JobParametersBuilder()
				.addLong("runAt", Instant.now().toEpochMilli())
				.addLong("limit", (long) limit)
				.addString("force", Boolean.toString(force))
				.toJobParameters());
			SyncStatistics statistics = (SyncStatistics) execution.getExecutionContext().get(GithubReadmeSyncJobConfig.STATISTICS_KEY);
			if (statistics == null) {
				statistics = new SyncStatistics();
			}
			syncLog.complete(statistics);
			syncLogRepository.save(syncLog);
			log.info(
				"githubReadmeSyncJob completed: searched={}, saved={}, readmes={}, related={}, skipped={}, failed={}",
				statistics.getSearchedCount(),
				statistics.getSavedCount(),
				statistics.getReadmeFetchedCount(),
				statistics.getAgentRelatedCount(),
				statistics.getSkippedCount(),
				statistics.getFailedCount()
			);
			return execution;
		} catch (Exception exception) {
			SyncStatistics statistics = new SyncStatistics();
			syncLog.fail(statistics, exception.getMessage());
			syncLogRepository.save(syncLog);
			log.error("githubReadmeSyncJob failed", exception);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GITHUB_SYNC_500", "GitHub 동기화 실행에 실패했습니다.");
		} finally {
			running.set(false);
		}
	}
}
