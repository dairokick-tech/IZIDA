# IZIDA Backend — Paso 11

API persistente de cuenta respaldada por PostgreSQL.

## Endpoints
- GET /health
- GET /api/v1/accounts/{accountId}
- GET /api/v1/accounts/{accountId}/movements?limit=50

El saldo se calcula desde las entradas del ledger de doble partida. El cliente nunca proporciona el saldo.

## Variables
Requeridas:
- DATABASE_URL
- DATABASE_USER
- DATABASE_PASSWORD

Opcionales:
- DATABASE_POOL_SIZE
- PORT

## Cuenta de desarrollo
00000000-0000-0000-0000-000000000001

El seed crea la cuenta con saldo cero. No se fabrica dinero.

## Estado
Paso 11 crea la primera API de lectura persistente. La app Android todavía conserva su modo local mientras terminamos la configuración de URL, TLS, autenticación y sincronización.

Antes de producción faltan autenticación/autorización, TLS, rate limiting, auditoría, migraciones, reconciliación y el endpoint transaccional de transferencias.
