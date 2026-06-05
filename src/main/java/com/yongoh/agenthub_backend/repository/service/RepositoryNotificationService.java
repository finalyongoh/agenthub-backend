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
import com.yongoh.agenthub_backend.repository.dto.RepositoryNotificationListResponse;
import com.yongoh.agenthub_backend.repository.dto.RepositoryNotificationDto;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryChangeLog;
import com.yongoh.agenthub_backend.repository.model.RepositoryNotification;
import com.yongoh.agenthub_backend.repository.repository.RepositoryBookmarkJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryChangeLogJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryNotificationJpaRepository;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.repository.UserRepository;

@Service
public class RepositoryNotificationService {
	private final UserRepository userRepository;
	private final RepositoryBookmarkJpaRepository bookmarkJpaRepository;
	private final RepositoryNotificationJpaRepository notificationJpaRepository;
	private final RepositoryChangeLogJpaRepository changeLogJpaRepository;

	public RepositoryNotificationService(
		UserRepository userRepository,
		RepositoryBookmarkJpaRepository bookmarkJpaRepository,
		RepositoryNotificationJpaRepository notificationJpaRepository,
		RepositoryChangeLogJpaRepository changeLogJpaRepository
	) {
		this.userRepository = userRepository;
		this.bookmarkJpaRepository = bookmarkJpaRepository;
		this.notificationJpaRepository = notificationJpaRepository;
		this.changeLogJpaRepository = changeLogJpaRepository;
	}

	@Transactional
	public void notifyChanged(AgentRepository repository, String changeType, String fieldName, String oldValue, String newValue, String oldSha, String newSha) {
		changeLogJpaRepository.save(RepositoryChangeLog.create(repository, changeType, fieldName, oldValue, newValue, oldSha, newSha));
		bookmarkJpaRepository.findByRepository(repository)
			.forEach(bookmark -> saveNotificationIfAbsent(bookmark.getUser(), repository, changeType, fieldName, oldSha, newSha));
	}

	@Transactional(readOnly = true)
	public List<RepositoryNotificationDto> findNotifications(UUID userId, Boolean read) {
		User user = findUser(userId);
		List<RepositoryNotification> notifications = read == null
			? notificationJpaRepository.findByUserOrderByCreatedAtDesc(user)
			: notificationJpaRepository.findByUserAndReadOrderByCreatedAtDesc(user, read);
		return notifications.stream()
			.map(RepositoryNotificationDto::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public RepositoryNotificationListResponse findNotifications(UUID userId, Boolean read, int page, int limit) {
		User user = findUser(userId);
		PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "createdAt"));
		var notifications = read == null
			? notificationJpaRepository.findByUser(user, pageRequest)
			: notificationJpaRepository.findByUserAndRead(user, read, pageRequest);
		return new RepositoryNotificationListResponse(
			notifications.stream()
				.map(RepositoryNotificationDto::from)
				.toList(),
			page,
			limit,
			notifications.getTotalElements()
		);
	}

	@Transactional
	public RepositoryNotificationDto markAsRead(UUID userId, UUID notificationId) {
		User user = findUser(userId);
		RepositoryNotification notification = notificationJpaRepository.findByIdAndUser(notificationId, user)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOTIFICATION_404", "알림을 찾을 수 없습니다."));
		notification.markAsRead();
		return RepositoryNotificationDto.from(notification);
	}

	@Transactional
	public void markAllAsRead(UUID userId) {
		User user = findUser(userId);
		notificationJpaRepository.findByUserAndRead(user, false)
			.forEach(RepositoryNotification::markAsRead);
	}

	@Transactional(readOnly = true)
	public long countUnread(UUID userId) {
		return notificationJpaRepository.countByUserAndRead(findUser(userId), false);
	}

	private void saveNotificationIfAbsent(User user, AgentRepository repository, String type, String fieldName, String oldSha, String newSha) {
		String changeKey = RepositoryNotification.changeKey(fieldName, newSha);
		if (notificationJpaRepository.existsByUserAndRepositoryAndTypeAndChangeKey(user, repository, type, changeKey)) {
			return;
		}
		try {
			notificationJpaRepository.save(RepositoryNotification.changed(user, repository, type, fieldName, oldSha, newSha));
		} catch (DataIntegrityViolationException ignored) {
			// 같은 변경사항을 동시에 감지한 경우 unique constraint가 최종 중복 방지 역할을 한다.
		}
	}

	private User findUser(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."));
	}
}
