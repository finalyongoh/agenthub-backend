package com.yongoh.agenthub_backend.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommunityLikeDto {
	private boolean liked;
	private long likeCount;
}
