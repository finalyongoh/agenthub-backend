package com.yongoh.agenthub_backend.community.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.community.model.RepositoryDiscussion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryDiscussionDto {
	private UUID id;
	private UUID userId;
	private UUID repositoryId;
	private String title;
	private String body;
	private boolean hasImage;
	private String imageUrl;
	private String status;
	private Instant createdAt;
	private Instant updatedAt;

	public static RepositoryDiscussionDto from(RepositoryDiscussion discussion) {
		return new RepositoryDiscussionDto(
			discussion.getId(),
			discussion.getUser().getId(),
			discussion.getRepositoryId(),
			discussion.getTitle(),
			discussion.getBody(),
			discussion.getImageFilename() != null,
			discussion.getImageFilename() == null ? null : "/api/images/" + discussion.getImageFilename(),
			discussion.getStatus().name(),
			discussion.getCreatedAt(),
			discussion.getUpdatedAt()
		);
	}
}
