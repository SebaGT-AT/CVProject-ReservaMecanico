CREATE TABLE specialties (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE professional_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    slug VARCHAR(80) NOT NULL UNIQUE,
    bio VARCHAR(1000),
    phone VARCHAR(30),
    time_zone VARCHAR(60) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE professional_specialties (
    professional_id UUID NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    specialty_id UUID NOT NULL REFERENCES specialties(id),
    PRIMARY KEY (professional_id, specialty_id)
);

CREATE TABLE service_offerings (
    id UUID PRIMARY KEY,
    professional_id UUID NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes BETWEEN 10 AND 720),
    price_amount NUMERIC(12, 2) NOT NULL CHECK (price_amount >= 0),
    currency CHAR(3) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_professional_profiles_published ON professional_profiles (published) WHERE published = TRUE;
CREATE INDEX ix_service_offerings_professional ON service_offerings (professional_id, active);

INSERT INTO specialties (id, name, slug, active) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Consultoría', 'consultoria', TRUE),
    ('10000000-0000-0000-0000-000000000002', 'Salud y bienestar', 'salud-y-bienestar', TRUE),
    ('10000000-0000-0000-0000-000000000003', 'Belleza y cuidado personal', 'belleza-y-cuidado-personal', TRUE),
    ('10000000-0000-0000-0000-000000000004', 'Educación', 'educacion', TRUE),
    ('10000000-0000-0000-0000-000000000005', 'Servicios tecnicos', 'servicios-tecnicos', TRUE),
    ('10000000-0000-0000-0000-000000000006', 'Mecánica automotriz', 'mecanica-automotriz', TRUE);
