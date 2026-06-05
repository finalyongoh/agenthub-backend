package com.yongoh.agenthub_backend.repository.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.repository.model.AgentRepository;
import com.yongoh.agenthub_backend.repository.model.RepositoryBookmark;
import com.yongoh.agenthub_backend.user.model.User;

public interface RepositoryBookmarkJpaRepository extends JpaRepository<RepositoryBookmark, UUID> {
	List<RepositoryBookmark> findByUserOrderByCreatedAtDesc(User user);

	Page<RepositoryBookmark> findByUser(User user, Pageable pageable);

	List<RepositoryBookmark> findByRepository(AgentRepository repository);

	Optional<RepositoryBookmark> findByUserAndRepository(User user, AgentRepository repository);

	boolean existsByUserAndRepository(User user, AgentRepository repository);

	void deleteByUserAndRepository(User user, AgentRepository repository);
}
