package com.yongoh.agenthub_backend.repository.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.OffsetDateTime;

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
    private UUID analysisId;

    private UUID repositoryId;
    private UUID snapshotId;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "jsonb")
    private String resultJson;

    private String errorMessage;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
