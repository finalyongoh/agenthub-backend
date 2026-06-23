package com.yongoh.agenthub_backend.community.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.community.model.DiscussionLike;
import com.yongoh.agenthub_backend.community.model.RepositoryDiscussion;
import com.yongoh.agenthub_backend.user.model.User;

public interface DiscussionLikeRepository extends JpaRepository<DiscussionLike, UUID> {
	Optional<DiscussionLike> findByDiscussionAndUser(RepositoryDiscussion discussion, User user);

	long countByDiscussion(RepositoryDiscussion discussion);
}
