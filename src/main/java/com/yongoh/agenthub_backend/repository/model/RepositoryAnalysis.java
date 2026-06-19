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
    private UUID analysisId;

    private UUID repositoryId;
    private UUID snapshotId;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "jsonb")
    private String resultJson;

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
