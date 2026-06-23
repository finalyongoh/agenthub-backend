package com.yongoh.agenthub_backend.community.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.community.model.Post;
import com.yongoh.agenthub_backend.community.model.PostLike;
import com.yongoh.agenthub_backend.user.model.User;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
	Optional<PostLike> findByPostAndUser(Post post, User user);

	long countByPost(Post post);
}
