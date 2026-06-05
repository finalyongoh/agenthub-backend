package com.yongoh.agenthub_backend.repository.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yongoh.agenthub_backend.repository.dto.RepositoryAnalysisResponse;
import com.yongoh.agenthub_backend.repository.dto.RepositoryDetailDto;
import com.yongoh.agenthub_backend.repository.dto.RepositoryListResponse;
import com.yongoh.agenthub_backend.repository.service.RepositoryQueryService;

@RestController
public class RepositoryController {
	private final RepositoryQueryService repositoryQueryService;

	public RepositoryController(RepositoryQueryService repositoryQueryService) {
		this.repositoryQueryService = repositoryQueryService;
	}

	@GetMapping("/api/repositories")
	public RepositoryListResponse findRepositories(
		@RequestParam(required = false) String category,
		@RequestParam(required = false) String language,
		@RequestParam(required = false) Integer minStars,
		@RequestParam(required = false) String sort,
		@RequestParam(required = false) String order,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "20") int limit
	) {
		return repositoryQueryService.findRepositories(category, language, minStars, sort, order, page, limit);
	}

	@GetMapping("/api/repositories/{repositoryId}")
	public RepositoryDetailDto findRepository(@PathVariable UUID repositoryId) {
		return repositoryQueryService.findRepository(repositoryId);
	}

	@PostMapping("/api/repositories/{repositoryId}/analysis")
	public RepositoryAnalysisResponse requestAnalysis(@PathVariable UUID repositoryId) {
		return repositoryQueryService.requestAnalysis(repositoryId);
	}
}
