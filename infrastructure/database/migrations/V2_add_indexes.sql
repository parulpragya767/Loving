-- Chat messages
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_created_at
    ON chat_messages (session_id, created_at);

-- Chat sessions
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_updated_at
    ON chat_sessions (user_id, updated_at DESC);

-- Ritual history
CREATE INDEX IF NOT EXISTS idx_ritual_history_user_updated_at
    ON ritual_history (user_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_ritual_history_user_status_updated_at
    ON ritual_history (user_id, status, updated_at DESC);

-- Ritual recommendations
CREATE INDEX IF NOT EXISTS idx_ritual_recommendations_user_created_at
    ON ritual_recommendations (user_id, created_at DESC);

-- User context
CREATE INDEX IF NOT EXISTS idx_user_contexts_user_conversation
    ON user_contexts (user_id, conversation_id);

-- Rituals
CREATE INDEX IF NOT EXISTS idx_rituals_published_updated_at
    ON rituals (updated_at DESC)
    WHERE status = 'PUBLISHED';

CREATE INDEX IF NOT EXISTS idx_rituals_published_title
    ON rituals (title)
    WHERE status = 'PUBLISHED';

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
 
CREATE INDEX IF NOT EXISTS idx_rituals_search_trgm
    ON rituals 
    USING gin ((coalesce(title,'') || ' ' || coalesce(tag_line,'') || ' ' || coalesce(description,'')) gin_trgm_ops)
    WHERE status = 'PUBLISHED';