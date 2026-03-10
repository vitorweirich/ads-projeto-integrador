CREATE TABLE REFRESH_TOKENS (
    id UUID PRIMARY KEY,
    user_id BIGINT,
    family_id TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    expires_at TIMESTAMP(6) WITH TIME ZONE,
    revoked BOOLEAN,
    replaced_by TEXT,
    user_agent TEXT,
    ip_address TEXT
);