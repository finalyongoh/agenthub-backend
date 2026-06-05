package com.yongoh.agenthub_backend.repository.dto;

import java.util.List;

public record BatchSyncLogListResponse(List<BatchSyncLogDto> items, int page, int limit, long total) {
}
