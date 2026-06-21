CREATE INDEX ix_appointments_professional_customer_start
    ON appointments (professional_id, customer_id, start_at)
    WHERE status <> 'CANCELLED';
