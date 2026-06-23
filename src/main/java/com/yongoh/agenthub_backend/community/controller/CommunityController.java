package com.yongoh.agenthub_backend.community.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.yongoh.agenthub_backend.community.dto.CommunityCommentDto;
import com.yongoh.agenthub_backend.community.dto.CommunityCommentRequest;
import com.yongoh.agenthub_backend.community.dto.CommunityCreateRequest;
import com.yongoh.agenthub_backend.community.dto.CommunityLikeDto;
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

	@PostMapping(value = "/api/posts", consumes = MediaType.APPLICATION_JSON_VALUE)
	public PostDto createPost(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestBody CommunityCreateRequest request
	) {
		return communityService.createPost(user.getId(), request);
	}

	@PostMapping(value = "/api/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public PostDto createPostWithImage(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestParam String title,
		@RequestParam String body,
		@RequestParam(required = false) MultipartFile image
	) {
		CommunityCreateRequest request = new CommunityCreateRequest();
		request.setTitle(title);
		request.setBody(body);
		return communityService.createPost(user.getId(), request, image);
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

	@GetMapping("/api/posts/{postId}/comments")
	public List<CommunityCommentDto> findPostComments(@PathVariable UUID postId) {
		return communityService.findPostComments(postId);
	}

	@PostMapping("/api/posts/{postId}/comments")
	public CommunityCommentDto createPostComment(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID postId,
		@RequestBody CommunityCommentRequest request
	) {
		return communityService.createPostComment(user.getId(), postId, request);
	}

	@GetMapping("/api/posts/{postId}/like")
	public CommunityLikeDto findPostLike(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID postId
	) {
		return communityService.findPostLike(user.getId(), postId);
	}

	@PostMapping("/api/posts/{postId}/like")
	public CommunityLikeDto togglePostLike(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID postId
	) {
		return communityService.togglePostLike(user.getId(), postId);
	}

	@PostMapping(value = "/api/repositories/{repositoryId}/discussions", consumes = MediaType.APPLICATION_JSON_VALUE)
	public RepositoryDiscussionDto createDiscussion(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId,
		@RequestBody CommunityCreateRequest request
	) {
		return communityService.createDiscussion(user.getId(), repositoryId, request);
	}

	@PostMapping(value = "/api/repositories/{repositoryId}/discussions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public RepositoryDiscussionDto createDiscussionWithImage(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId,
		@RequestParam String title,
		@RequestParam String body,
		@RequestParam(required = false) MultipartFile image
	) {
		CommunityCreateRequest request = new CommunityCreateRequest();
		request.setTitle(title);
		request.setBody(body);
		return communityService.createDiscussion(user.getId(), repositoryId, request, image);
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

	@GetMapping("/api/repositories/{repositoryId}/discussions/{discussionId}/comments")
	public List<CommunityCommentDto> findDiscussionComments(
		@PathVariable UUID repositoryId,
		@PathVariable UUID discussionId
	) {
		return communityService.findDiscussionComments(repositoryId, discussionId);
	}

	@PostMapping("/api/repositories/{repositoryId}/discussions/{discussionId}/comments")
	public CommunityCommentDto createDiscussionComment(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId,
		@PathVariable UUID discussionId,
		@RequestBody CommunityCommentRequest request
	) {
		return communityService.createDiscussionComment(user.getId(), repositoryId, discussionId, request);
	}

	@GetMapping("/api/repositories/{repositoryId}/discussions/{discussionId}/like")
	public CommunityLikeDto findDiscussionLike(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId,
		@PathVariable UUID discussionId
	) {
		return communityService.findDiscussionLike(user.getId(), repositoryId, discussionId);
	}

	@PostMapping("/api/repositories/{repositoryId}/discussions/{discussionId}/like")
	public CommunityLikeDto toggleDiscussionLike(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId,
		@PathVariable UUID discussionId
	) {
		return communityService.toggleDiscussionLike(user.getId(), repositoryId, discussionId);
	}
}
