-- PostgreSQL DDL for rituals table aligned with com.lovingapp.loving.model.Ritual
-- Note: Hibernate currently manages schema (spring.jpa.hibernate.ddl-auto=update).
-- This DDL is provided for manual database setup/migrations if needed.

CREATE TABLE IF NOT EXISTS rituals (
    id uuid PRIMARY KEY,
    title varchar(255) NOT NULL,
    short_description varchar(255),
    full_description text,
    ritual_types jsonb NOT NULL DEFAULT '[]'::jsonb,
    ritual_mode varchar(20) CHECK (ritual_mode IN ('SOLO','PARTNER','GROUP')),
    tone jsonb NOT NULL DEFAULT '[]'::jsonb,
    sensitivity_level varchar(20) CHECK (sensitivity_level IN ('LOW','MODERATE','HIGH')),
    effort_level varchar(20) CHECK (effort_level IN ('LOW','MODERATE','HIGH')),
    estimated_duration varchar(50),
    ritual_steps jsonb,
    media_assets jsonb,
    love_types_supported jsonb NOT NULL DEFAULT '[]'::jsonb,
    emotional_states_supported jsonb NOT NULL DEFAULT '[]'::jsonb,
    relational_needs_served jsonb NOT NULL DEFAULT '[]'::jsonb,
    life_contexts_relevant jsonb NOT NULL DEFAULT '[]'::jsonb,
    rhythm varchar(30) CHECK (rhythm IN ('DAILY','WEEKLY','OCCASIONAL','EVENT_TRIGGERED')),
    preparation_requirements jsonb NOT NULL DEFAULT '[]'::jsonb,
    semantic_summary text,
    related_rituals jsonb NOT NULL DEFAULT '[]'::jsonb,
    status varchar(20) CHECK (status IN ('PUBLISHED','DRAFT','ARCHIVED')),
    created_by varchar(100),
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now()
);

-- Helpful indexes for jsonb containment queries
CREATE INDEX IF NOT EXISTS idx_rituals_ritual_types_gin ON rituals USING gin (ritual_types);
CREATE INDEX IF NOT EXISTS idx_rituals_tone_gin ON rituals USING gin (tone);
CREATE INDEX IF NOT EXISTS idx_rituals_love_types_supported_gin ON rituals USING gin (love_types_supported);
CREATE INDEX IF NOT EXISTS idx_rituals_emotional_states_supported_gin ON rituals USING gin (emotional_states_supported);
CREATE INDEX IF NOT EXISTS idx_rituals_relational_needs_served_gin ON rituals USING gin (relational_needs_served);
CREATE INDEX IF NOT EXISTS idx_rituals_life_contexts_relevant_gin ON rituals USING gin (life_contexts_relevant);
