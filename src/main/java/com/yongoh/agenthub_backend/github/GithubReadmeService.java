package com.yongoh.agenthub_backend.github;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yongoh.agenthub_backend.github.dto.GithubReadmeDto;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;

@Service
public class GithubReadmeService {
	private final GithubClient githubClient;

	public GithubReadmeService(GithubClient githubClient) {
		this.githubClient = githubClient;
	}

	public Optional<GithubReadmeDto> findReadme(AgentRepository repository) {
		return githubClient.findReadme(repository.getOwner(), repository.getName());
	}
}
