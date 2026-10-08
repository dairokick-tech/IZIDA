# IZIDA — Paso 7: Pago mediante QR

Flujo implementado:

1. Validar QR IZIDA.
2. Identificar destinatario.
3. Continuar al pago.
4. Ingresar monto.
5. Revisar destinatario y monto.
6. Autorizar la operación.
7. Ejecutar TransactionService.
8. Mostrar ID de operación.

El pago usa el mismo motor de transacciones del Paso 4, con débito/crédito e idempotencia.

## Estado de esta etapa

Es un flujo de desarrollo. El ledger utilizado por esta pantalla es en memoria y la cuenta demo comienza sin saldo. Por ello, una autorización puede terminar correctamente rechazada por fondos insuficientes; esto es intencional y evita inventar dinero.

## Pendiente para producción

- Integrar cámara real para lectura QR.
- Backend como fuente de verdad.
- PIN/biometría validada por backend.
- QR firmado y con protección contra replay.
- PostgreSQL y transacciones ACID.
- Límites y antifraude.
- Comprobante persistente.
- Auditoría y conciliación.
