package com.yongoh.agenthub_backend.github.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubReadmeDto {
	private String path;
	private String sha;
	private String content;
}
