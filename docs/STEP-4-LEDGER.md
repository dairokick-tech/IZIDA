# IZIDA — Paso 4: Motor financiero y Ledger

Este paso incorpora la base del motor financiero.

## Principios
- El saldo no se modifica desde la UI.
- Una transferencia genera dos asientos: débito y crédito.
- Cada operación tiene un identificador único.
- Se exige una clave de idempotencia.
- Una clave repetida no debe duplicar la operación.
- Se valida saldo suficiente antes de registrar el débito.
- Los estados contemplan PENDING, PROCESSED, REJECTED y REVERSED.

## Implementación actual
La aplicación contiene un Ledger en memoria únicamente para pruebas de dominio/UI. No es una base financiera de producción.

La siguiente etapa debe sustituirlo por un backend con PostgreSQL, transacciones ACID, bloqueo/concurrencia, auditoría, reconciliación y persistencia durable.

## Regla de producción
El cliente Android nunca debe ser la fuente de verdad del saldo. El backend/ledger será la autoridad.
