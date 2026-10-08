CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES izida_users(id),
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT accounts_currency_chk CHECK (currency = 'PEN'),
    CONSTRAINT accounts_status_chk CHECK (status IN ('ACTIVE','BLOCKED','PENDING','CLOSED'))
);

CREATE TABLE IF NOT EXISTS ledger_transactions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(120) NOT NULL UNIQUE,
    source_account_id UUID NOT NULL REFERENCES accounts(id),
    destination_account_id UUID NOT NULL REFERENCES accounts(id),
    amount NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference VARCHAR(160),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT transactions_status_chk CHECK (status IN ('PENDING','PROCESSED','REJECTED','REVERSED')),
    CONSTRAINT transactions_currency_chk CHECK (currency = 'PEN'),
    CONSTRAINT transactions_accounts_chk CHECK (source_account_id <> destination_account_id)
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES ledger_transactions(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    entry_type VARCHAR(10) NOT NULL,
    amount NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT entries_type_chk CHECK (entry_type IN ('DEBIT','CREDIT')),
    CONSTRAINT entries_currency_chk CHECK (currency = 'PEN')
);

CREATE INDEX IF NOT EXISTS idx_ledger_entries_account_created
    ON ledger_entries(account_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_transactions_source_created
    ON ledger_transactions(source_account_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_transactions_destination_created
    ON ledger_transactions(destination_account_id, created_at DESC);
