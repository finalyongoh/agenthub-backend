package com.yongoh.agenthub_backend.community.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.community.model.Post;
import com.yongoh.agenthub_backend.community.model.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
	List<PostComment> findByPostOrderByCreatedAtAsc(Post post);
}
