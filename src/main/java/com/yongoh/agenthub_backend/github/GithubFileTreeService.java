package com.yongoh.agenthub_backend.github;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yongoh.agenthub_backend.github.dto.GithubFileTreeItemDto;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;

@Service
public class GithubFileTreeService {
	private final GithubClient githubClient;

	public GithubFileTreeService(GithubClient githubClient) {
		this.githubClient = githubClient;
	}

	public List<GithubFileTreeItemDto> findShallowFileTree(AgentRepository repository) {
		String branch = StringUtils.hasText(repository.getDefaultBranch()) ? repository.getDefaultBranch() : "main";
		return githubClient.findFileTree(repository.getOwner(), repository.getName(), branch);
	}
}
