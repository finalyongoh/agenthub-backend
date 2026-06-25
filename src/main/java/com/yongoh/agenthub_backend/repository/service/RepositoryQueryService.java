package com.yongoh.agenthub_backend.repository.service;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.repository.dto.RepositoryAnalysisResponse;
import com.yongoh.agenthub_backend.repository.dto.RepositoryDetailDto;
import com.yongoh.agenthub_backend.repository.dto.RepositoryListResponse;
import com.yongoh.agenthub_backend.repository.dto.RepositorySummaryDto;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryAnalysis;
import com.yongoh.agenthub_backend.repository.repository.AgentTraceAnalysisResultRepository;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryReadmeJpaRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class RepositoryQueryService {
	private static final int MAX_PAGE_SIZE = 15;

	private final AgentRepositoryJpaRepository repositoryJpaRepository;
	private final RepositoryReadmeJpaRepository readmeJpaRepository;
	private final RepositoryAnalysisRepository analysisRepository;
	private final AgentTraceAnalysisResultRepository agentTraceAnalysisResultRepository;
	private final AnalysisService analysisService;

	public RepositoryQueryService(
		AgentRepositoryJpaRepository repositoryJpaRepository,
		RepositoryReadmeJpaRepository readmeJpaRepository,
		RepositoryAnalysisRepository analysisRepository,
		AgentTraceAnalysisResultRepository agentTraceAnalysisResultRepository,
		AnalysisService analysisService
	) {
		this.repositoryJpaRepository = repositoryJpaRepository;
		this.readmeJpaRepository = readmeJpaRepository;
		this.analysisRepository = analysisRepository;
		this.agentTraceAnalysisResultRepository = agentTraceAnalysisResultRepository;
		this.analysisService = analysisService;
	}

	@Transactional(readOnly = true)
	public RepositoryListResponse findRepositories(String keyword, String category, String language, Integer minStars, String sort, String order, int page, int limit) {
		int pageNumber = Math.max(page - 1, 0);
		int pageSize = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
		PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort(sort, order));
		Specification<AgentRepository> specification = (root, query, builder) -> {
			Predicate predicate = builder.and(
				builder.isFalse(root.get("archived")),
				builder.isFalse(root.get("fork")),
				builder.isTrue(root.get("agentRelated"))
			);
			if (category != null && !category.isBlank()) {
				predicate = builder.and(predicate, builder.equal(root.get("agentCategory"), category));
			}
			if (language != null && !language.isBlank()) {
				predicate = builder.and(predicate, builder.equal(root.get("language"), language));
			}
			if (keyword != null && !keyword.isBlank()) {
				String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
				Predicate keywordPredicate = builder.or(
					builder.like(builder.lower(root.get("fullName")), likeKeyword),
					builder.like(builder.lower(root.get("owner")), likeKeyword),
					builder.like(builder.lower(root.get("name")), likeKeyword),
					builder.like(builder.lower(builder.coalesce(root.get("description"), "")), likeKeyword),
					builder.like(builder.lower(builder.coalesce(root.get("descriptionKo"), "")), likeKeyword),
					builder.like(builder.lower(builder.coalesce(root.get("readmeSummary"), "")), likeKeyword),
					builder.like(builder.lower(builder.coalesce(root.get("topics"), "")), likeKeyword),
					builder.like(builder.lower(builder.coalesce(root.get("language"), "")), likeKeyword),
					builder.like(builder.lower(builder.coalesce(root.get("agentCategory"), "")), likeKeyword)
				);
				predicate = builder.and(predicate, keywordPredicate);
			}
			if (minStars != null) {
				predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("stars"), minStars));
			}
			return predicate;
		};
		var repositories = repositoryJpaRepository.findAll(specification, pageRequest);
		return new RepositoryListResponse(
			repositories.stream()
				.map(repository -> RepositorySummaryDto.from(repository, analysisRepository.existsByRepositoryId(repository.getId())))
				.toList(),
			pageNumber + 1,
			pageSize,
			repositories.getTotalElements()
		);
	}

	@Transactional(readOnly = true)
	public RepositoryDetailDto findRepository(UUID repositoryId) {
		AgentRepository repository = findRepositoryOrThrow(repositoryId);
		var readme = readmeJpaRepository.findByRepository(repository).orElse(null);
		var analysis = analysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId).orElse(null);
		return RepositoryDetailDto.from(repository, readme, analysis);
	}

	public RepositoryAnalysisResponse requestAnalysis(UUID repositoryId) {
		AgentRepository repository = findRepositoryOrThrow(repositoryId);
		readmeJpaRepository.findByRepository(repository)
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "REPOSITORY_001", "README가 없는 레포지토리는 분석을 요청할 수 없습니다."));
		var latestAnalysis = analysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId);
		if (latestAnalysis.isPresent() && isActiveAnalysisStatus(latestAnalysis.get().getStatus())) {
			return agentTraceAnalysisResultRepository.findByAnalysisId(latestAnalysis.get().getAnalysisId())
				.orElseGet(() -> RepositoryAnalysisResponse.from(latestAnalysis.get()));
		}
		UUID snapshotId = UUID.randomUUID();
		RepositoryAnalysis analysis = analysisService.requestAnalysis(repositoryId, snapshotId, null, repository.getHtmlUrl());
		return RepositoryAnalysisResponse.from(analysis);
	}

	@Transactional(readOnly = true)
	public RepositoryAnalysisResponse findLatestAnalysis(UUID repositoryId) {
		findRepositoryOrThrow(repositoryId);
		
		var backendResult = analysisRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId)
			.map(analysis -> agentTraceAnalysisResultRepository.findByAnalysisId(analysis.getAnalysisId())
				.orElseGet(() -> RepositoryAnalysisResponse.from(analysis)));
				
		var traceResult = agentTraceAnalysisResultRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId);
		
		if (backendResult.isEmpty() && traceResult.isEmpty()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "ANALYSIS_404", "분석 결과를 찾을 수 없습니다.");
		}
		if (backendResult.isEmpty()) {
			return traceResult.get();
		}
		if (traceResult.isEmpty()) {
			return backendResult.get();
		}
		
		var backendTime = backendResult.get().getCreatedAt();
		var traceTime = traceResult.get().getCreatedAt();
		
		if (backendTime != null && traceTime != null && traceTime.isAfter(backendTime)) {
			return traceResult.get();
		}
		return backendResult.get();
	}

	private AgentRepository findRepositoryOrThrow(UUID repositoryId) {
		return repositoryJpaRepository.findById(repositoryId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPOSITORY_404", "레포지토리를 찾을 수 없습니다."));
	}

	private boolean isActiveAnalysisStatus(String status) {
		return "QUEUED".equalsIgnoreCase(status) || "RUNNING".equalsIgnoreCase(status);
	}

	private Sort sort(String sort, String order) {
		String property = switch (sort == null ? "" : sort) {
			case "updated" -> "pushedAt";
			case "fetched" -> "lastFetchedAt";
			case "score" -> "agentScore";
			default -> "stars";
		};
		Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
		return Sort.by(direction, property);
	}
}
