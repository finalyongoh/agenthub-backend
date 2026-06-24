package com.yongoh.agenthub_backend.moderation.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.yongoh.agenthub_backend.community.repository.PostRepository;
import com.yongoh.agenthub_backend.community.repository.PostCommentRepository;
import com.yongoh.agenthub_backend.community.repository.DiscussionCommentRepository;
import com.yongoh.agenthub_backend.community.repository.RepositoryDiscussionRepository;
import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;
import com.yongoh.agenthub_backend.moderation.dto.ContentReportRequest;
import com.yongoh.agenthub_backend.moderation.dto.ContentReportResponse;
import com.yongoh.agenthub_backend.moderation.model.ContentReport;
import com.yongoh.agenthub_backend.moderation.repository.ContentReportRepository;
import com.yongoh.agenthub_backend.repository.repository.RepositoryAnalysisRepository;
import com.yongoh.agenthub_backend.user.model.User;
import com.yongoh.agenthub_backend.user.repository.UserRepository;

@Service
public class ContentReportService {
	private static final int MAX_CATEGORY_LENGTH = 80;
	private static final int MAX_REASON_LENGTH = 2000;

	private final ContentReportRepository reportRepository;
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final PostCommentRepository postCommentRepository;
	private final RepositoryDiscussionRepository discussionRepository;
	private final DiscussionCommentRepository discussionCommentRepository;
	private final RepositoryAnalysisRepository analysisRepository;

	public ContentReportService(
		ContentReportRepository reportRepository,
		UserRepository userRepository,
		PostRepository postRepository,
		PostCommentRepository postCommentRepository,
		RepositoryDiscussionRepository discussionRepository,
		DiscussionCommentRepository discussionCommentRepository,
		RepositoryAnalysisRepository analysisRepository
	) {
		this.reportRepository = reportRepository;
		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.postCommentRepository = postCommentRepository;
		this.discussionRepository = discussionRepository;
		this.discussionCommentRepository = discussionCommentRepository;
		this.analysisRepository = analysisRepository;
	}

	@Transactional
	public ContentReportResponse createReport(UUID reporterId, ContentReportRequest request) {
		validateRequest(request);
		User reporter = findActiveUser(reporterId);
		validateTargetExists(request);

		reportRepository.findByReporterAndTargetTypeAndTargetId(reporter, request.getTargetType(), request.getTargetId())
			.ifPresent(report -> {
				throw new ApiException(HttpStatus.CONFLICT, "REPORT_409", "이미 신고한 항목입니다.");
			});

		ContentReport report = ContentReport.create(
			reporter,
			request.getTargetType(),
			request.getTargetId(),
			trimToLimit(request.getCategory(), MAX_CATEGORY_LENGTH),
			trimToLimit(request.getReason(), MAX_REASON_LENGTH)
		);
		return ContentReportResponse.from(reportRepository.save(report));
	}

	private void validateRequest(ContentReportRequest request) {
		if (request == null
			|| request.getTargetType() == null
			|| request.getTargetId() == null
			|| !StringUtils.hasText(request.getCategory())
			|| !StringUtils.hasText(request.getReason())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "REPORT_400", "신고 항목과 신고 사유를 입력해주세요.");
		}
	}

	private void validateTargetExists(ContentReportRequest request) {
		boolean exists = switch (request.getTargetType()) {
			case POST -> postRepository.existsById(request.getTargetId());
			case POST_COMMENT -> postCommentRepository.existsById(request.getTargetId());
			case DISCUSSION -> discussionRepository.existsById(request.getTargetId());
			case DISCUSSION_COMMENT -> discussionCommentRepository.existsById(request.getTargetId());
			case REPOSITORY_ANALYSIS -> analysisRepository.existsById(request.getTargetId());
		};

		if (!exists) {
			throw new ApiException(HttpStatus.NOT_FOUND, "REPORT_404", "신고 대상을 찾을 수 없습니다.");
		}
	}

	private User findActiveUser(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."));
		if (user.isRestricted()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_403", "제한된 사용자입니다.");
		}
		return user;
	}

	private String trimToLimit(String value, int limit) {
		String trimmed = value.trim();
		return trimmed.length() <= limit ? trimmed : trimmed.substring(0, limit);
	}
}
