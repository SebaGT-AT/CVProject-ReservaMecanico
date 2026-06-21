CREATE TABLE notification_outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    deduplication_key VARCHAR(160) NOT NULL UNIQUE,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'DEAD')),
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    locked_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    last_error VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_notification_outbox_dispatch
    ON notification_outbox (status, next_attempt_at, created_at)
    WHERE status IN ('PENDING', 'FAILED', 'PROCESSING');
