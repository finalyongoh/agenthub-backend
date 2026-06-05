package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.RepositoryBookmark;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryBookmarkDto {
	private UUID id;
	private RepositorySummaryDto repository;
	private Instant createdAt;

	public static RepositoryBookmarkDto from(RepositoryBookmark bookmark, boolean hasAnalysis) {
		return new RepositoryBookmarkDto(
			bookmark.getId(),
			RepositorySummaryDto.from(bookmark.getRepository(), hasAnalysis),
			bookmark.getCreatedAt()
		);
	}
}
