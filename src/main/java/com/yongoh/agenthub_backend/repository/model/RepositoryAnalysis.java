package com.yongoh.agenthub_backend.repository.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "repository_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "analysis_id")
    private UUID analysisId;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "snapshot_id")
    private UUID snapshotId;

    @Column(length = 40, nullable = false)
    private String status;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String resultJson;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
