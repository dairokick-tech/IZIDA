CREATE TABLE IF NOT EXISTS izida_users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    phone VARCHAR(9) NOT NULL UNIQUE,
    pin_salt BYTEA NOT NULL,
    pin_hash BYTEA NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT users_phone_chk CHECK (phone ~ '^[0-9]{9}$'),
    CONSTRAINT users_status_chk CHECK (status IN ('ACTIVE','BLOCKED','PENDING','CLOSED'))
);

CREATE TABLE IF NOT EXISTS auth_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES izida_users(id),
    token_hash BYTEA NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_auth_sessions_user ON auth_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_expiry ON auth_sessions(expires_at);
