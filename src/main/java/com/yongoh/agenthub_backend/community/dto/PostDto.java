package com.yongoh.agenthub_backend.community.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.community.model.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostDto {
	private UUID id;
	private UUID userId;
	private String title;
	private String body;
	private boolean hasImage;
	private String imageUrl;
	private String status;
	private Instant createdAt;
	private Instant updatedAt;

	public static PostDto from(Post post) {
		return new PostDto(
			post.getId(),
			post.getUser().getId(),
			post.getTitle(),
			post.getBody(),
			post.getImageFilename() != null,
			post.getImageFilename() == null ? null : "/api/images/" + post.getImageFilename(),
			post.getStatus().name(),
			post.getCreatedAt(),
			post.getUpdatedAt()
		);
	}
}
