# IZIDA Backend — Paso 10

Foundation for the persistent IZIDA backend.

## Included

- Ktor API service.
- PostgreSQL driver.
- HikariCP and Exposed dependencies.
- Health endpoint: GET /health.
- PostgreSQL schema for accounts, financial transactions and double-entry ledger entries.
- Unique idempotency key.
- Transaction states: PENDING, PROCESSED, REJECTED, REVERSED.
- Database constraints for positive amounts, currency and account separation.

## Configuration

No credentials are stored in Git. Database configuration must be supplied through environment variables in the next backend integration step.

## Important

This step establishes the persistent backend boundary and database model. It does not claim that IZIDA is connected to a bank, Yape, Plin or any other external financial participant.

The production transfer endpoint must perform balance validation and double-entry posting inside one PostgreSQL transaction, with row locking/serializable isolation, authorization, limits, audit logging and reconciliation.
