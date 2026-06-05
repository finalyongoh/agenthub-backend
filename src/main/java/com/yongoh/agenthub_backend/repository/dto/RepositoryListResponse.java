package com.yongoh.agenthub_backend.repository.dto;

import java.util.List;

public record RepositoryListResponse(List<RepositorySummaryDto> items, int page, int limit, long total) {
}
