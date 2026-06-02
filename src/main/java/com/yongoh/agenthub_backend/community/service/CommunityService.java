package com.yongoh.agenthub_backend.community.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.yongoh.agenthub_backend.community.dto.CommunityCreateRequest;
import com.yongoh.agenthub_backend.community.dto.PostDto;
import com.yongoh.agenthub_backend.community.dto.RepositoryDiscussionDto;
import com.yongoh.agenthub_backend.community.model.Post;
import com.yongoh.agenthub_backend.community.model.RepositoryDiscussion;
import com.yongoh.agenthub_backend.community.repository.PostRepository;
import com.yongoh.agenthub_backend.community.repository.RepositoryDiscussionRepository;
import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.repository.UserRepository;

@Service
public class CommunityService {
	private final PostRepository postRepository;
	private final RepositoryDiscussionRepository discussionRepository;
	private final UserRepository userRepository;

	public CommunityService(
		PostRepository postRepository,
		RepositoryDiscussionRepository discussionRepository,
		UserRepository userRepository
	) {
		this.postRepository = postRepository;
		this.discussionRepository = discussionRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public PostDto createPost(UUID userId, CommunityCreateRequest request) {
		validateRequest(request);
		User user = findActiveUser(userId);
		Post post = Post.create(user, request.getTitle().trim(), request.getBody().trim());
		return PostDto.from(postRepository.save(post));
	}

	@Transactional(readOnly = true)
	public List<PostDto> findPosts() {
		return postRepository.findAllByOrderByCreatedAtDesc()
			.stream()
			.map(PostDto::from)
			.toList();
	}

	@Transactional
	public PostDto deletePost(UUID userId, UUID postId) {
		findActiveUser(userId);
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMUNITY_404", "게시글을 찾을 수 없습니다."));
		validateOwner(userId, post.getUser().getId());
		post.delete();
		return PostDto.from(post);
	}

	@Transactional
	public RepositoryDiscussionDto createDiscussion(UUID userId, UUID repositoryId, CommunityCreateRequest request) {
		validateRequest(request);
		User user = findActiveUser(userId);
		RepositoryDiscussion discussion = RepositoryDiscussion.create(
			user,
			repositoryId,
			request.getTitle().trim(),
			request.getBody().trim()
		);
		return RepositoryDiscussionDto.from(discussionRepository.save(discussion));
	}

	@Transactional(readOnly = true)
	public List<RepositoryDiscussionDto> findDiscussions(UUID repositoryId) {
		return discussionRepository.findByRepositoryIdOrderByCreatedAtDesc(repositoryId)
			.stream()
			.map(RepositoryDiscussionDto::from)
			.toList();
	}

	@Transactional
	public RepositoryDiscussionDto deleteDiscussion(UUID userId, UUID repositoryId, UUID discussionId) {
		findActiveUser(userId);
		RepositoryDiscussion discussion = discussionRepository.findById(discussionId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMUNITY_404", "토론 글을 찾을 수 없습니다."));
		if (!discussion.getRepositoryId().equals(repositoryId)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "COMMUNITY_404", "토론 글을 찾을 수 없습니다.");
		}
		validateOwner(userId, discussion.getUser().getId());
		discussion.delete();
		return RepositoryDiscussionDto.from(discussion);
	}

	private User findActiveUser(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."));
		if (user.isRestricted()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_403", "제한된 사용자입니다.");
		}
		return user;
	}

	private void validateRequest(CommunityCreateRequest request) {
		if (request == null || !StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getBody())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "COMMUNITY_001", "필수 입력값이 누락되었습니다.");
		}
	}

	private void validateOwner(UUID requestUserId, UUID ownerUserId) {
		if (!requestUserId.equals(ownerUserId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "COMMUNITY_403", "작성자만 삭제할 수 있습니다.");
		}
	}
}
