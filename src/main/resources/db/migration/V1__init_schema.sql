CREATE TABLE chat_messages (
    created_at timestamptz NOT NULL,
    id uuid NOT NULL,
    session_id uuid NOT NULL,
    role varchar(20) NOT NULL CHECK (role IN ('USER','ASSISTANT','SYSTEM')),
    content text NOT NULL,
    metadata jsonb,
    PRIMARY KEY (id)
);

CREATE TABLE chat_sessions (
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    title varchar(120),
    last_message_preview varchar(160),
    PRIMARY KEY (id)
);

CREATE TABLE love_types (
    id integer NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    love_type varchar(20) NOT NULL UNIQUE CHECK (love_type IN ('BELONG','FIRE','SPARK','CARE','SELF','BUILD','GROW','BEYOND','GRACE')),
    title varchar(30) NOT NULL,
    content_hash varchar(128) NOT NULL,
    description text,
    subtitle varchar(255) NOT NULL,
    sections jsonb NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ritual_history (
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    id uuid NOT NULL,
    recommendation_id uuid,
    ritual_id uuid NOT NULL,
    ritual_pack_id uuid,
    user_id uuid NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('SUGGESTED','ACTIVE','STARTED','COMPLETED','SKIPPED','ABANDONED')),
    feedback varchar(30) CHECK (feedback IN ('WARM','JOYFUL','CALM','NEUTRAL','SAD','FRUSTRATED','ENERGIZED')),
    PRIMARY KEY (id)
);

CREATE TABLE ritual_pack_rituals (
    pack_id uuid NOT NULL,
    ritual_id uuid NOT NULL
);

CREATE TABLE ritual_packs (
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    id uuid NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('PUBLISHED','DRAFT','ARCHIVED')),
    journey varchar(40) CHECK (journey IN ('FEELING_DISTANT','LOVE_FEELS_FLAT','LOST_TOUCH','CARRYING_TOO_MUCH','WEATHERING_A_STORM','BRIDGING_THE_DIVIDE','LEARNING_TO_HEAR_EACH_OTHER','MAKING_SPACE_FOR_US','KEEP_THE_LOVE_ALIVE','GROW_AND_EVOLVE_TOGETHER','RETURN_TO_SELF','CELEBRATE_US')),
    content_hash varchar(128),
    title varchar(200) NOT NULL,
    short_description text,
    description text,
    how_it_helps text,
    semantic_summary text,
    tag_line varchar(255),
    love_types jsonb NOT NULL,
    media_assets jsonb NOT NULL,
    relational_needs jsonb NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ritual_recommendations (
    created_at timestamptz NOT NULL,
    id uuid NOT NULL,
    ritual_pack_id uuid NOT NULL,
    source_id uuid,
    user_id uuid NOT NULL,
    source varchar(20) NOT NULL CHECK (source IN ('CHAT','WEEKLY')),
    status varchar(20) NOT NULL CHECK (status IN ('SUGGESTED','VIEWED','ADDED','SKIPPED')),
    PRIMARY KEY (id)
);

CREATE TABLE rituals (
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    id uuid NOT NULL,
    ritual_mode varchar(20) NOT NULL CHECK (ritual_mode IN ('SOLO','TOGETHER')),
    status varchar(20) NOT NULL CHECK (status IN ('PUBLISHED','DRAFT','ARCHIVED')),
    time_taken varchar(20) CHECK (time_taken IN ('MOMENT','SHORT','MEDIUM','LONG','EXTENDED','FLEXIBLE')),
    content_hash varchar(128),
    title varchar(200) NOT NULL,
    short_description text,
    description text,
    how_it_helps text,
    semantic_summary text,
    tag_line varchar(255),
    love_types jsonb NOT NULL,
    media_assets jsonb NOT NULL,
    relational_needs jsonb NOT NULL,
    ritual_tones jsonb NOT NULL,
    steps jsonb NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_contexts (
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    conversation_id uuid,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    relationship_status varchar(30) CHECK (relationship_status IN ('NEW','ESTABLISHED','COMMITTED','ENGAGED','MARRIED','REKINDLING','LONG_DISTANCE','CASUAL','EXPLORING','OTHER')),
    journey varchar(40) CHECK (journey IN ('FEELING_DISTANT','LOVE_FEELS_FLAT','LOST_TOUCH','CARRYING_TOO_MUCH','WEATHERING_A_STORM','BRIDGING_THE_DIVIDE','LEARNING_TO_HEAR_EACH_OTHER','MAKING_SPACE_FOR_US','KEEP_THE_LOVE_ALIVE','GROW_AND_EVOLVE_TOGETHER','RETURN_TO_SELF','CELEBRATE_US')),
    semantic_summary text,
    love_types jsonb,
    relational_needs jsonb,
    PRIMARY KEY (id)
);

CREATE TABLE user_usage_counters (
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    period_type varchar(20) NOT NULL CHECK (period_type IN ('DAILY','WEEKLY')),
    period_start timestamptz NOT NULL,
    ai_messages_count integer NOT NULL DEFAULT 0,
    recommendations_count integer NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (user_id, period_type, period_start)
);

CREATE TABLE users (
    is_beta_user boolean NOT NULL, 
    onboarding_completed boolean NOT NULL, 
    created_at timestamptz NOT NULL, 
    last_login_at timestamptz, 
    subscription_expires_at timestamptz, 
    subscription_started_at timestamptz, 
    updated_at timestamptz NOT NULL, 
    auth_user_id uuid NOT NULL UNIQUE, 
    id uuid NOT NULL, 
    display_name varchar(120), 
    email varchar(255) NOT NULL UNIQUE, 
    subscription_source varchar(30) NOT NULL CHECK (subscription_source IN ('APPLE','GOOGLE_PLAY','STRIPE','INTERNAL','NONE')), 
    subscription_status varchar(30) NOT NULL CHECK (subscription_status IN ('INACTIVE','ACTIVE','TRIALING','GRACE_PERIOD','EXPIRED','CANCELLED')), 
    subscription_tier varchar(30) NOT NULL CHECK (subscription_tier IN ('FREE','PREMIUM')), 
    PRIMARY KEY (id));

ALTER TABLE ritual_pack_rituals
    ADD CONSTRAINT ritual_pack_rituals_pk
    PRIMARY KEY (pack_id, ritual_id);

ALTER TABLE ritual_pack_rituals
    ADD CONSTRAINT fk_pack
    FOREIGN KEY (pack_id)
    REFERENCES ritual_packs(id)
    ON DELETE CASCADE;

ALTER TABLE ritual_pack_rituals
    ADD CONSTRAINT fk_ritual
    FOREIGN KEY (ritual_id)
    REFERENCES rituals(id)
    ON DELETE CASCADE;

ALTER TABLE chat_sessions
    ADD CONSTRAINT fk_chat_sessions_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

ALTER TABLE chat_messages
    ADD CONSTRAINT fk_chat_messages_session
    FOREIGN KEY (session_id)
    REFERENCES chat_sessions(id)
    ON DELETE CASCADE;

ALTER TABLE ritual_recommendations
    ADD CONSTRAINT fk_ritual_recommendations_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

ALTER TABLE ritual_recommendations
    ADD CONSTRAINT fk_ritual_recommendations_pack
    FOREIGN KEY (ritual_pack_id)
    REFERENCES ritual_packs(id);

ALTER TABLE ritual_history
    ADD CONSTRAINT fk_ritual_history_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

ALTER TABLE ritual_history
    ADD CONSTRAINT fk_ritual_history_ritual
    FOREIGN KEY (ritual_id)
    REFERENCES rituals(id);

ALTER TABLE ritual_history
    ADD CONSTRAINT fk_ritual_history_pack
    FOREIGN KEY (ritual_pack_id)
    REFERENCES ritual_packs(id)
    ON DELETE SET NULL;

ALTER TABLE ritual_history
    ADD CONSTRAINT fk_ritual_history_recommendation
    FOREIGN KEY (recommendation_id)
    REFERENCES ritual_recommendations(id)
    ON DELETE SET NULL;

ALTER TABLE user_contexts
    ADD CONSTRAINT fk_user_contexts_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

ALTER TABLE user_contexts
    ADD CONSTRAINT fk_user_contexts_conversation
    FOREIGN KEY (conversation_id)
    REFERENCES chat_sessions(id)
    ON DELETE CASCADE;

ALTER TABLE user_usage_counters
    ADD CONSTRAINT fk_user_usage_counters_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;