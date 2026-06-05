package com.yongoh.agenthub_backend.repository.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.repository.model.RepositoryNotification;
import com.yongoh.agenthub_backend.user.model.User;

public interface RepositoryNotificationJpaRepository extends JpaRepository<RepositoryNotification, UUID> {
	List<RepositoryNotification> findByUserOrderByCreatedAtDesc(User user);

	List<RepositoryNotification> findByUserAndReadOrderByCreatedAtDesc(User user, boolean read);

	Page<RepositoryNotification> findByUser(User user, Pageable pageable);

	Page<RepositoryNotification> findByUserAndRead(User user, boolean read, Pageable pageable);

	Optional<RepositoryNotification> findByIdAndUser(UUID id, User user);

	boolean existsByUserAndRepositoryAndTypeAndChangeKey(User user, com.yongoh.agenthub_backend.repository.model.AgentRepository repository, String type, String changeKey);

	long countByUserAndRead(User user, boolean read);

	List<RepositoryNotification> findByUserAndRead(User user, boolean read);
}
