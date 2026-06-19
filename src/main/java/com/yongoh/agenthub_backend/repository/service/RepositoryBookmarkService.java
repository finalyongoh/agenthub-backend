package com.yongoh.agenthub_backend.repository.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.repository.dto.RepositoryBookmarkDto;
import com.yongoh.agenthub_backend.repository.dto.RepositoryBookmarkListResponse;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryBookmark;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryBookmarkJpaRepository;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.repository.UserRepository;

@Service
public class RepositoryBookmarkService {
	private final UserRepository userRepository;
	private final AgentRepositoryJpaRepository repositoryJpaRepository;
	private final RepositoryBookmarkJpaRepository bookmarkJpaRepository;
	private final RepositoryAnalysisRepository analysisRepository;

	public RepositoryBookmarkService(
		UserRepository userRepository,
		AgentRepositoryJpaRepository repositoryJpaRepository,
		RepositoryBookmarkJpaRepository bookmarkJpaRepository,
		RepositoryAnalysisRepository analysisRepository
	) {
		this.userRepository = userRepository;
		this.repositoryJpaRepository = repositoryJpaRepository;
		this.bookmarkJpaRepository = bookmarkJpaRepository;
		this.analysisRepository = analysisRepository;
	}

	@Transactional
	public RepositoryBookmarkDto bookmark(UUID userId, UUID repositoryId) {
		User user = findUser(userId);
		AgentRepository repository = findRepository(repositoryId);
		RepositoryBookmark bookmark = bookmarkJpaRepository.findByUserAndRepository(user, repository)
			.orElseGet(() -> saveBookmarkSafely(user, repository));
		return RepositoryBookmarkDto.from(bookmark, analysisRepository.existsByRepositoryId(repository.getId()));
	}

	@Transactional
	public void deleteBookmark(UUID userId, UUID repositoryId) {
		User user = findUser(userId);
		AgentRepository repository = findRepository(repositoryId);
		bookmarkJpaRepository.deleteByUserAndRepository(user, repository);
	}

	@Transactional(readOnly = true)
	public List<RepositoryBookmarkDto> findBookmarks(UUID userId) {
		User user = findUser(userId);
		return bookmarkJpaRepository.findByUserOrderByCreatedAtDesc(user)
			.stream()
			.map(bookmark -> RepositoryBookmarkDto.from(
				bookmark,
				analysisRepository.existsByRepositoryId(bookmark.getRepository().getId())
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public RepositoryBookmarkListResponse findBookmarks(UUID userId, int page, int limit) {
		User user = findUser(userId);
		PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "createdAt"));
		var bookmarks = bookmarkJpaRepository.findByUser(user, pageRequest);
		return new RepositoryBookmarkListResponse(
			bookmarks.stream()
				.map(bookmark -> RepositoryBookmarkDto.from(
					bookmark,
					analysisRepository.existsByRepositoryId(bookmark.getRepository().getId())
				))
				.toList(),
			page,
			limit,
			bookmarks.getTotalElements()
		);
	}

	private RepositoryBookmark saveBookmarkSafely(User user, AgentRepository repository) {
		try {
			return bookmarkJpaRepository.saveAndFlush(RepositoryBookmark.create(user, repository));
		} catch (DataIntegrityViolationException exception) {
			return bookmarkJpaRepository.findByUserAndRepository(user, repository)
				.orElseThrow(() -> exception);
		}
	}

	private User findUser(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."));
	}

	private AgentRepository findRepository(UUID repositoryId) {
		return repositoryJpaRepository.findById(repositoryId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPOSITORY_404", "수집된 레포지토리를 찾을 수 없습니다."));
	}
}
