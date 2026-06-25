package com.yongoh.agenthub_backend.repository.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.yongoh.agenthub_backend.repository.dto.RepositoryAnalysisResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class AgentTraceAnalysisResultRepository {
	private final JdbcTemplate jdbcTemplate;

	public AgentTraceAnalysisResultRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<RepositoryAnalysisResponse> findByAnalysisId(UUID analysisId) {
		return query(
			"""
				SELECT a.analysis_id, a.repository_id, a.snapshot_id, a.status, a.result_json::text AS result_json,
				       NULL AS error_message, a.created_at, a.updated_at,
				       r.title AS report_title, r.body_markdown AS report_body_markdown
				FROM agenttrace_repository_analyses a
				LEFT JOIN analysis_reports r ON r.analysis_id = a.analysis_id AND r.lang = 'ko'
				WHERE a.analysis_id = ?::uuid
				LIMIT 1
				""",
			analysisId.toString()
		);
	}

	public Optional<RepositoryAnalysisResponse> findFirstByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId) {
		return query(
			"""
				SELECT a.analysis_id, a.repository_id, a.snapshot_id, a.status, a.result_json::text AS result_json,
				       NULL AS error_message, a.created_at, a.updated_at,
				       r.title AS report_title, r.body_markdown AS report_body_markdown
				FROM agenttrace_repository_analyses a
				LEFT JOIN analysis_reports r ON r.analysis_id = a.analysis_id AND r.lang = 'ko'
				WHERE a.repository_id = ?::uuid
				ORDER BY a.created_at DESC
				LIMIT 1
				""",
			repositoryId.toString()
		);
	}

	private Optional<RepositoryAnalysisResponse> query(String sql, String value) {
		try {
			List<RepositoryAnalysisResponse> results = jdbcTemplate.query(sql, this::mapRow, value);
			return results.stream().findFirst();
		} catch (DataAccessException exception) {
			log.warn("Failed to read AgentTrace analysis result.", exception);
			return Optional.empty();
		}
	}

	private RepositoryAnalysisResponse mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
		String snapshotIdStr = resultSet.getString("snapshot_id");
		return new RepositoryAnalysisResponse(
			UUID.fromString(resultSet.getString("analysis_id")),
			UUID.fromString(resultSet.getString("repository_id")),
			snapshotIdStr != null ? UUID.fromString(snapshotIdStr) : null,
			resultSet.getString("status"),
			resultSet.getString("result_json"),
			resultSet.getString("error_message"),
			toInstant(resultSet.getTimestamp("created_at")),
			toInstant(resultSet.getTimestamp("updated_at")),
			resultSet.getString("report_title"),
			resultSet.getString("report_body_markdown")
		);
	}

	private Instant toInstant(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toInstant();
	}
}
