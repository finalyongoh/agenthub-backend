package com.yongoh.agenthub_backend.batch;

import java.util.List;
import java.util.UUID;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.yongoh.agenthub_backend.global.config.GithubProperties;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.service.RepositorySyncService;
import com.yongoh.agenthub_backend.repository.service.SyncStatistics;

@Configuration
public class GithubReadmeSyncJobConfig {
	public static final String JOB_NAME = "githubReadmeSyncJob";
	static final String REPOSITORIES_KEY = "repositories";
	static final String STATISTICS_KEY = "statistics";

	@Bean
	Job githubReadmeSyncJob(JobRepository jobRepository, Step searchAgentRepositoriesStep, Step fetchReadmesStep, Step scoreAgentRepositoriesStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
			.start(searchAgentRepositoriesStep)
			.next(fetchReadmesStep)
			.next(scoreAgentRepositoriesStep)
			.build();
	}

	@Bean
	Step searchAgentRepositoriesStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		RepositorySyncService repositorySyncService,
		GithubProperties properties
	) {
		return new StepBuilder("searchAgentRepositoriesStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				SyncStatistics statistics = new SyncStatistics();
				int limit = chunkContext.getStepContext().getJobParameters().get("limit") instanceof Number value
					? value.intValue()
					: properties.getSync().getMaxRepositoriesPerRun();
				List<UUID> repositoryIds = repositorySyncService.searchAndSaveCandidates(limit, statistics)
					.stream()
					.map(AgentRepository::getId)
					.toList();
				chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(REPOSITORIES_KEY, repositoryIds);
				chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STATISTICS_KEY, statistics);
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	Step fetchReadmesStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		RepositorySyncService repositorySyncService,
		AgentRepositoryJpaRepository repositoryJpaRepository
	) {
		return new StepBuilder("fetchReadmesStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				var context = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
				@SuppressWarnings("unchecked")
				List<UUID> repositoryIds = (List<UUID>) context.get(REPOSITORIES_KEY);
				SyncStatistics statistics = (SyncStatistics) context.get(STATISTICS_KEY);
				boolean force = Boolean.parseBoolean(String.valueOf(chunkContext.getStepContext().getJobParameters().getOrDefault("force", false)));
				List<AgentRepository> repositories = repositoryJpaRepository.findAllById(repositoryIds);
				repositorySyncService.fetchReadmes(repositories, force, statistics);
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	Step scoreAgentRepositoriesStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		RepositorySyncService repositorySyncService,
		AgentRepositoryJpaRepository repositoryJpaRepository
	) {
		return new StepBuilder("scoreAgentRepositoriesStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				var context = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
				@SuppressWarnings("unchecked")
				List<UUID> repositoryIds = (List<UUID>) context.get(REPOSITORIES_KEY);
				SyncStatistics statistics = (SyncStatistics) context.get(STATISTICS_KEY);
				List<AgentRepository> repositories = repositoryJpaRepository.findAllById(repositoryIds);
				repositorySyncService.scoreRepositories(repositories, statistics);
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}
}
