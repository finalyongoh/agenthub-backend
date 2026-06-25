package com.yongoh.agenthub_backend.community.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.yongoh.agenthub_backend.community.dto.CommunityCreateRequest;
import com.yongoh.agenthub_backend.community.dto.CommunityCommentDto;
import com.yongoh.agenthub_backend.community.dto.CommunityCommentRequest;
import com.yongoh.agenthub_backend.community.dto.CommunityLikeDto;
import com.yongoh.agenthub_backend.community.dto.PostDto;
import com.yongoh.agenthub_backend.community.dto.RepositoryDiscussionDto;
import com.yongoh.agenthub_backend.community.model.DiscussionComment;
import com.yongoh.agenthub_backend.community.model.DiscussionLike;
import com.yongoh.agenthub_backend.community.model.Post;
import com.yongoh.agenthub_backend.community.model.PostComment;
import com.yongoh.agenthub_backend.community.model.PostLike;
import com.yongoh.agenthub_backend.community.model.PostStatus;
import com.yongoh.agenthub_backend.community.model.RepositoryDiscussion;
import com.yongoh.agenthub_backend.community.repository.DiscussionCommentRepository;
import com.yongoh.agenthub_backend.community.repository.DiscussionLikeRepository;
import com.yongoh.agenthub_backend.community.repository.PostCommentRepository;
import com.yongoh.agenthub_backend.community.repository.PostLikeRepository;
import com.yongoh.agenthub_backend.community.repository.PostRepository;
import com.yongoh.agenthub_backend.community.repository.RepositoryDiscussionRepository;
import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.repository.UserRepository;

@Service
public class CommunityService {
	private final PostRepository postRepository;
	private final RepositoryDiscussionRepository discussionRepository;
	private final PostCommentRepository postCommentRepository;
	private final DiscussionCommentRepository discussionCommentRepository;
	private final PostLikeRepository postLikeRepository;
	private final DiscussionLikeRepository discussionLikeRepository;
	private final UserRepository userRepository;
	private final AgentRepositoryJpaRepository agentRepositoryJpaRepository;
	private final PostImageStorageService postImageStorageService;

	public CommunityService(
		PostRepository postRepository,
		RepositoryDiscussionRepository discussionRepository,
		PostCommentRepository postCommentRepository,
		DiscussionCommentRepository discussionCommentRepository,
		PostLikeRepository postLikeRepository,
		DiscussionLikeRepository discussionLikeRepository,
		UserRepository userRepository,
		AgentRepositoryJpaRepository agentRepositoryJpaRepository,
		PostImageStorageService postImageStorageService
	) {
		this.postRepository = postRepository;
		this.discussionRepository = discussionRepository;
		this.postCommentRepository = postCommentRepository;
		this.discussionCommentRepository = discussionCommentRepository;
		this.postLikeRepository = postLikeRepository;
		this.discussionLikeRepository = discussionLikeRepository;
		this.userRepository = userRepository;
		this.agentRepositoryJpaRepository = agentRepositoryJpaRepository;
		this.postImageStorageService = postImageStorageService;
	}

	@Transactional
	public PostDto createPost(UUID userId, CommunityCreateRequest request) {
		return createPost(userId, request, null);
	}

	@Transactional
	public PostDto createPost(UUID userId, CommunityCreateRequest request, MultipartFile image) {
		validateRequest(request);
		User user = findActiveUser(userId);
		String imageFilename = postImageStorageService.store(image);
		Post post = Post.create(user, request.getTitle().trim(), request.getBody().trim(), imageFilename);
		return PostDto.from(postRepository.save(post));
	}

	@Transactional(readOnly = true)
	public List<PostDto> findPosts() {
		return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE)
			.stream()
			.map(PostDto::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public PostDto findPostDetail(UUID postId) {
		return PostDto.from(findPost(postId));
	}

	@Transactional
	public PostDto updatePost(UUID userId, UUID postId, CommunityCreateRequest request) {
		return updatePost(userId, postId, request, null);
	}

	@Transactional
	public PostDto updatePost(UUID userId, UUID postId, CommunityCreateRequest request, MultipartFile image) {
		validateRequest(request);
		findActiveUser(userId);
		Post post = findPost(postId);
		validateOwner(userId, post.getUser().getId(), "작성자만 수정할 수 있습니다.");
		String imageFilename = postImageStorageService.store(image);
		post.update(request.getTitle().trim(), request.getBody().trim(), imageFilename);
		return PostDto.from(post);
	}

	@Transactional
	public PostDto deletePost(UUID userId, UUID postId) {
		findActiveUser(userId);
		Post post = findPost(postId);
		validateOwner(userId, post.getUser().getId(), "작성자만 삭제할 수 있습니다.");
		post.delete();
		return PostDto.from(post);
	}

	@Transactional(readOnly = true)
	public List<CommunityCommentDto> findPostComments(UUID postId) {
		Post post = findPost(postId);
		return postCommentRepository.findByPostOrderByCreatedAtAsc(post)
			.stream()
			.map(CommunityCommentDto::from)
			.toList();
	}

	@Transactional
	public CommunityCommentDto createPostComment(UUID userId, UUID postId, CommunityCommentRequest request) {
		User user = findActiveUser(userId);
		Post post = findPost(postId);
		String body = validateCommentRequest(request);
		return CommunityCommentDto.from(postCommentRepository.save(PostComment.create(post, user, body)));
	}

	@Transactional(readOnly = true)
	public CommunityLikeDto findPostLike(UUID userId, UUID postId) {
		Post post = findPost(postId);
		User user = findActiveUser(userId);
		return new CommunityLikeDto(
			postLikeRepository.findByPostAndUser(post, user).isPresent(),
			postLikeRepository.countByPost(post)
		);
	}

	@Transactional
	public CommunityLikeDto togglePostLike(UUID userId, UUID postId) {
		User user = findActiveUser(userId);
		Post post = findPost(postId);
		var like = postLikeRepository.findByPostAndUser(post, user);
		boolean liked;
		if (like.isPresent()) {
			postLikeRepository.delete(like.get());
			liked = false;
		} else {
			postLikeRepository.save(PostLike.create(post, user));
			liked = true;
		}
		return new CommunityLikeDto(liked, postLikeRepository.countByPost(post));
	}

	@Transactional
	public RepositoryDiscussionDto createDiscussion(UUID userId, UUID repositoryId, CommunityCreateRequest request) {
		return createDiscussion(userId, repositoryId, request, null);
	}

	@Transactional
	public RepositoryDiscussionDto createDiscussion(
		UUID userId,
		UUID repositoryId,
		CommunityCreateRequest request,
		MultipartFile image
	) {
		validateRequest(request);
		User user = findActiveUser(userId);
		validateCollectedRepository(repositoryId);
		RepositoryDiscussion discussion = RepositoryDiscussion.create(
			user,
			repositoryId,
			request.getTitle().trim(),
			request.getBody().trim(),
			postImageStorageService.store(image)
		);
		return RepositoryDiscussionDto.from(discussionRepository.save(discussion));
	}

	@Transactional(readOnly = true)
	public List<RepositoryDiscussionDto> findDiscussions(UUID repositoryId) {
		validateCollectedRepository(repositoryId);
		return discussionRepository.findByRepositoryIdOrderByCreatedAtDesc(repositoryId)
			.stream()
			.map(RepositoryDiscussionDto::from)
			.toList();
	}

	@Transactional
	public RepositoryDiscussionDto deleteDiscussion(UUID userId, UUID repositoryId, UUID discussionId) {
		findActiveUser(userId);
		validateCollectedRepository(repositoryId);
		RepositoryDiscussion discussion = findDiscussion(repositoryId, discussionId);
		validateOwner(userId, discussion.getUser().getId(), "작성자만 삭제할 수 있습니다.");
		discussion.delete();
		return RepositoryDiscussionDto.from(discussion);
	}

	@Transactional(readOnly = true)
	public List<CommunityCommentDto> findDiscussionComments(UUID repositoryId, UUID discussionId) {
		RepositoryDiscussion discussion = findDiscussion(repositoryId, discussionId);
		return discussionCommentRepository.findByDiscussionOrderByCreatedAtAsc(discussion)
			.stream()
			.map(CommunityCommentDto::from)
			.toList();
	}

	@Transactional
	public CommunityCommentDto createDiscussionComment(UUID userId, UUID repositoryId, UUID discussionId, CommunityCommentRequest request) {
		User user = findActiveUser(userId);
		RepositoryDiscussion discussion = findDiscussion(repositoryId, discussionId);
		String body = validateCommentRequest(request);
		return CommunityCommentDto.from(discussionCommentRepository.save(DiscussionComment.create(discussion, user, body)));
	}

	@Transactional(readOnly = true)
	public CommunityLikeDto findDiscussionLike(UUID userId, UUID repositoryId, UUID discussionId) {
		User user = findActiveUser(userId);
		RepositoryDiscussion discussion = findDiscussion(repositoryId, discussionId);
		return new CommunityLikeDto(
			discussionLikeRepository.findByDiscussionAndUser(discussion, user).isPresent(),
			discussionLikeRepository.countByDiscussion(discussion)
		);
	}

	@Transactional
	public CommunityLikeDto toggleDiscussionLike(UUID userId, UUID repositoryId, UUID discussionId) {
		User user = findActiveUser(userId);
		RepositoryDiscussion discussion = findDiscussion(repositoryId, discussionId);
		var like = discussionLikeRepository.findByDiscussionAndUser(discussion, user);
		boolean liked;
		if (like.isPresent()) {
			discussionLikeRepository.delete(like.get());
			liked = false;
		} else {
			discussionLikeRepository.save(DiscussionLike.create(discussion, user));
			liked = true;
		}
		return new CommunityLikeDto(liked, discussionLikeRepository.countByDiscussion(discussion));
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

	private String validateCommentRequest(CommunityCommentRequest request) {
		if (request == null || !StringUtils.hasText(request.getBody())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "COMMUNITY_001", "댓글 내용을 입력해주세요.");
		}
		return request.getBody().trim();
	}

	private Post findPost(UUID postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMUNITY_404", "게시글을 찾을 수 없습니다."));
		if (post.getStatus() != PostStatus.ACTIVE) {
			throw new ApiException(HttpStatus.NOT_FOUND, "COMMUNITY_404", "게시글을 찾을 수 없습니다.");
		}
		return post;
	}

	private RepositoryDiscussion findDiscussion(UUID repositoryId, UUID discussionId) {
		validateCollectedRepository(repositoryId);
		RepositoryDiscussion discussion = discussionRepository.findById(discussionId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMUNITY_404", "토론 글을 찾을 수 없습니다."));
		if (!discussion.getRepositoryId().equals(repositoryId)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "COMMUNITY_404", "토론 글을 찾을 수 없습니다.");
		}
		return discussion;
	}

	private void validateCollectedRepository(UUID repositoryId) {
		if (!agentRepositoryJpaRepository.existsById(repositoryId)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "REPOSITORY_404", "수집된 레포지토리를 찾을 수 없습니다.");
		}
	}

	private void validateOwner(UUID requestUserId, UUID ownerUserId, String message) {
		if (!requestUserId.equals(ownerUserId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "COMMUNITY_403", message);
		}
	}
}
