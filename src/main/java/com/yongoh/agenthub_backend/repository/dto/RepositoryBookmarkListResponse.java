package com.yongoh.agenthub_backend.repository.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryBookmarkListResponse {
	private List<RepositoryBookmarkDto> items;
	private int page;
	private int limit;
	private long total;
}
