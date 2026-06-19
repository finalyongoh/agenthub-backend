package com.yongoh.agenthub_backend.community.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.community.model.DiscussionComment;
import com.yongoh.agenthub_backend.community.model.RepositoryDiscussion;

public interface DiscussionCommentRepository extends JpaRepository<DiscussionComment, UUID> {
	List<DiscussionComment> findByDiscussionOrderByCreatedAtAsc(RepositoryDiscussion discussion);
}
