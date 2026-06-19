package com.yongoh.agenthub_backend.github.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubFileTreeItemDto {
	private String path;
	private String type;
}
