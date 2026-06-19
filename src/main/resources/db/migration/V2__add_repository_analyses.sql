CREATE TABLE repository_analyses (
    analysis_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repository_id     UUID NOT NULL,
    snapshot_id       UUID NOT NULL,
    status            VARCHAR(40) NOT NULL DEFAULT 'QUEUED',
    result_json       JSONB,
    error_message     TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_analyses_repo_completed ON repository_analyses (repository_id, status);
CREATE INDEX idx_analyses_status ON repository_analyses (status);
