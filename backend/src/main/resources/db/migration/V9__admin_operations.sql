CREATE TABLE admin_audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL REFERENCES users(id),
    target_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(60) NOT NULL,
    detail VARCHAR(500),
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_admin_audit_actor_time ON admin_audit_events (actor_user_id, occurred_at DESC);
CREATE INDEX ix_admin_audit_target_time ON admin_audit_events (target_user_id, occurred_at DESC);
