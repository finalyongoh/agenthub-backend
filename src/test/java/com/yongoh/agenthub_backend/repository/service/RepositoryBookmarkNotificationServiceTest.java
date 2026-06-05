package com.yongoh.agenthub_backend.repository.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.yongoh.agenthub_backend.github.dto.GithubRepositoryDto;
import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.repository.AgentRepositoryJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryChangeLogJpaRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryNotificationJpaRepository;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.repository.UserRepository;

@SpringBootTest
class RepositoryBookmarkNotificationServiceTest {
	@Autowired
	RepositoryBookmarkService bookmarkService;

	@Autowired
	RepositoryNotificationService notificationService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AgentRepositoryJpaRepository repositoryJpaRepository;

	@Autowired
	RepositoryNotificationJpaRepository notificationJpaRepository;

	@Autowired
	RepositoryChangeLogJpaRepository changeLogJpaRepository;

	@Test
	@Transactional
	void bookmarkPaginationAndNotificationReadFlow() {
		User user = userRepository.save(User.create("bookmark-" + System.nanoTime() + "@example.com", "password", "bookmark"));
		AgentRepository repository = repositoryJpaRepository.save(repository(1L, "owner/bookmark-agent"));

		bookmarkService.bookmark(user.getId(), repository.getId());
		bookmarkService.bookmark(user.getId(), repository.getId());

		var bookmarks = bookmarkService.findBookmarks(user.getId(), 1, 20);
		assertThat(bookmarks.getTotal()).isEqualTo(1);
		assertThat(bookmarks.getItems()).hasSize(1);

		notificationService.notifyChanged(repository, "metadata_changed", "stars", "1", "2", null, "2");
		notificationService.notifyChanged(repository, "metadata_changed", "stars", "1", "2", null, "2");

		var notifications = notificationService.findNotifications(user.getId(), false, 1, 20);
		assertThat(notifications.getTotal()).isEqualTo(1);
		assertThat(notificationService.countUnread(user.getId())).isEqualTo(1);
		assertThat(changeLogJpaRepository.count()).isGreaterThanOrEqualTo(2);

		notificationService.markAsRead(user.getId(), notifications.getItems().get(0).getId());
		assertThat(notificationService.countUnread(user.getId())).isZero();

		notificationService.notifyChanged(repository, "readme_changed", "readmeSha", "old", "new", "old", "new");
		assertThat(notificationService.countUnread(user.getId())).isEqualTo(1);

		notificationService.markAllAsRead(user.getId());
		assertThat(notificationService.countUnread(user.getId())).isZero();
		assertThat(notificationJpaRepository.findByUserAndRead(user, false)).isEmpty();
	}

	private AgentRepository repository(Long githubId, String fullName) {
		String[] names = fullName.split("/");
		return AgentRepository.create(new GithubRepositoryDto(
			githubId,
			fullName,
			names[0],
			names[1],
			"AI agent repository",
			"https://github.com/" + fullName,
			null,
			null,
			"main",
			"Java",
			List.of("ai-agent"),
			100,
			10,
			10,
			1,
			"MIT",
			Instant.now(),
			Instant.now(),
			Instant.now(),
			false,
			false
		));
	}
}
