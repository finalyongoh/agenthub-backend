package com.yongoh.agenthub_backend.community.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.yongoh.agenthub_backend.community.dto.CommunityCreateRequest;
import com.yongoh.agenthub_backend.community.dto.PostDto;
import com.yongoh.agenthub_backend.community.dto.RepositoryDiscussionDto;
import com.yongoh.agenthub_backend.community.service.CommunityService;
import com.yongoh.agenthub_backend.global.security.AuthenticatedUser;

@RestController
public class CommunityController {
	private final CommunityService communityService;

	public CommunityController(CommunityService communityService) {
		this.communityService = communityService;
	}

	@PostMapping("/api/posts")
	public PostDto createPost(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestBody CommunityCreateRequest request
	) {
		return communityService.createPost(user.getId(), request);
	}

	@GetMapping("/api/posts")
	public List<PostDto> findPosts() {
		return communityService.findPosts();
	}

	@DeleteMapping("/api/posts/{postId}")
	public PostDto deletePost(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID postId
	) {
		return communityService.deletePost(user.getId(), postId);
	}

	@PostMapping("/api/repositories/{repositoryId}/discussions")
	public RepositoryDiscussionDto createDiscussion(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId,
		@RequestBody CommunityCreateRequest request
	) {
		return communityService.createDiscussion(user.getId(), repositoryId, request);
	}

	@GetMapping("/api/repositories/{repositoryId}/discussions")
	public List<RepositoryDiscussionDto> findDiscussions(@PathVariable UUID repositoryId) {
		return communityService.findDiscussions(repositoryId);
	}

	@DeleteMapping("/api/repositories/{repositoryId}/discussions/{discussionId}")
	public RepositoryDiscussionDto deleteDiscussion(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId,
		@PathVariable UUID discussionId
	) {
		return communityService.deleteDiscussion(user.getId(), repositoryId, discussionId);
	}
}
