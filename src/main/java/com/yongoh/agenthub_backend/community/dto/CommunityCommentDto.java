package com.yongoh.agenthub_backend.community.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.community.model.DiscussionComment;
import com.yongoh.agenthub_backend.community.model.PostComment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommunityCommentDto {
	private UUID id;
	private UUID userId;
	private String author;
	private String authorProfileImageUrl;
	private String body;
	private Instant createdAt;

	public static CommunityCommentDto from(PostComment comment) {
		return new CommunityCommentDto(
			comment.getId(),
			comment.getUser().getId(),
			comment.getUser().getNickname(),
			comment.getUser().getProfileImageFilename() == null ? null : "/api/images/" + comment.getUser().getProfileImageFilename(),
			comment.getBody(),
			comment.getCreatedAt()
		);
	}

	public static CommunityCommentDto from(DiscussionComment comment) {
		return new CommunityCommentDto(
			comment.getId(),
			comment.getUser().getId(),
			comment.getUser().getNickname(),
			comment.getUser().getProfileImageFilename() == null ? null : "/api/images/" + comment.getUser().getProfileImageFilename(),
			comment.getBody(),
			comment.getCreatedAt()
		);
	}
}
