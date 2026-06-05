package com.yongoh.agenthub_backend.repository.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yongoh.agenthub_backend.global.security.AuthenticatedUser;
import com.yongoh.agenthub_backend.repository.dto.RepositoryAnalysisResponse;
import com.yongoh.agenthub_backend.repository.dto.RepositoryBookmarkDto;
import com.yongoh.agenthub_backend.repository.dto.RepositoryBookmarkListResponse;
import com.yongoh.agenthub_backend.repository.dto.RepositoryDetailDto;
import com.yongoh.agenthub_backend.repository.dto.RepositoryListResponse;
import com.yongoh.agenthub_backend.repository.dto.RepositoryNotificationDto;
import com.yongoh.agenthub_backend.repository.dto.RepositoryNotificationListResponse;
import com.yongoh.agenthub_backend.repository.dto.UnreadNotificationCountResponse;
import com.yongoh.agenthub_backend.repository.service.RepositoryBookmarkService;
import com.yongoh.agenthub_backend.repository.service.RepositoryNotificationService;
import com.yongoh.agenthub_backend.repository.service.RepositoryQueryService;

@RestController
public class RepositoryController {
	private final RepositoryQueryService repositoryQueryService;
	private final RepositoryBookmarkService bookmarkService;
	private final RepositoryNotificationService notificationService;

	public RepositoryController(
		RepositoryQueryService repositoryQueryService,
		RepositoryBookmarkService bookmarkService,
		RepositoryNotificationService notificationService
	) {
		this.repositoryQueryService = repositoryQueryService;
		this.bookmarkService = bookmarkService;
		this.notificationService = notificationService;
	}

	@GetMapping("/api/repositories")
	public RepositoryListResponse findRepositories(
		@RequestParam(required = false) String category,
		@RequestParam(required = false) String language,
		@RequestParam(required = false) Integer minStars,
		@RequestParam(required = false) String sort,
		@RequestParam(required = false) String order,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "20") int limit
	) {
		return repositoryQueryService.findRepositories(category, language, minStars, sort, order, page, limit);
	}

	@GetMapping("/api/repositories/{repositoryId}")
	public RepositoryDetailDto findRepository(@PathVariable UUID repositoryId) {
		return repositoryQueryService.findRepository(repositoryId);
	}

	@PostMapping("/api/repositories/{repositoryId}/analysis")
	public RepositoryAnalysisResponse requestAnalysis(@PathVariable UUID repositoryId) {
		return repositoryQueryService.requestAnalysis(repositoryId);
	}

	@PostMapping("/api/repositories/{repositoryId}/bookmark")
	public RepositoryBookmarkDto bookmarkRepository(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId
	) {
		return bookmarkService.bookmark(user.getId(), repositoryId);
	}

	@DeleteMapping("/api/repositories/{repositoryId}/bookmark")
	public void deleteBookmark(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID repositoryId
	) {
		bookmarkService.deleteBookmark(user.getId(), repositoryId);
	}

	@GetMapping("/api/bookmarks")
	public RepositoryBookmarkListResponse findBookmarks(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "20") int limit
	) {
		return bookmarkService.findBookmarks(user.getId(), page, limit);
	}

	@GetMapping("/api/notifications")
	public RepositoryNotificationListResponse findNotifications(
		@AuthenticationPrincipal AuthenticatedUser user,
		@RequestParam(required = false) Boolean read,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "20") int limit
	) {
		return notificationService.findNotifications(user.getId(), read, page, limit);
	}

	@GetMapping("/api/notifications/unread-count")
	public UnreadNotificationCountResponse countUnreadNotifications(@AuthenticationPrincipal AuthenticatedUser user) {
		return new UnreadNotificationCountResponse(notificationService.countUnread(user.getId()));
	}

	@PostMapping("/api/notifications/{notificationId}/read")
	public RepositoryNotificationDto markNotificationAsRead(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID notificationId
	) {
		return notificationService.markAsRead(user.getId(), notificationId);
	}

	@PostMapping("/api/notifications/read-all")
	public void markAllNotificationsAsRead(@AuthenticationPrincipal AuthenticatedUser user) {
		notificationService.markAllAsRead(user.getId());
	}
}
