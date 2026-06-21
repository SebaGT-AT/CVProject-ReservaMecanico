CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE booking_policies
    ADD COLUMN cancellation_notice_minutes INTEGER NOT NULL DEFAULT 60
        CHECK (cancellation_notice_minutes BETWEEN 0 AND 43200);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    professional_id UUID NOT NULL REFERENCES professional_profiles(id),
    customer_id UUID NOT NULL REFERENCES users(id),
    service_id UUID NOT NULL REFERENCES service_offerings(id),
    idempotency_key UUID NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    busy_until TIMESTAMPTZ NOT NULL,
    professional_time_zone VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    service_name VARCHAR(120) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price_amount NUMERIC(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    cancellation_reason VARCHAR(500),
    cancelled_by UUID REFERENCES users(id),
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (start_at < end_at AND end_at <= busy_until),
    UNIQUE (customer_id, idempotency_key)
);

ALTER TABLE appointments ADD CONSTRAINT ex_appointments_professional_overlap
    EXCLUDE USING gist (
        professional_id WITH =,
        tstzrange(start_at, busy_until, '[)') WITH &&
    ) WHERE (status IN ('PENDING', 'CONFIRMED'));

CREATE INDEX ix_appointments_customer_start ON appointments (customer_id, start_at DESC);
CREATE INDEX ix_appointments_professional_start ON appointments (professional_id, start_at);

CREATE TABLE appointment_status_history (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    actor_user_id UUID NOT NULL REFERENCES users(id),
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_appointment_history_appointment_time
    ON appointment_status_history (appointment_id, created_at);
