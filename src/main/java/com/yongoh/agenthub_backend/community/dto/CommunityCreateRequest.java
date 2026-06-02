package com.yongoh.agenthub_backend.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityCreateRequest {
	private String title;
	private String body;
}
