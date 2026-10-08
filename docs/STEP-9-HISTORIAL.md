# IZIDA — Paso 9: historial financiero

Se conectó la cuenta demo a una instancia única de Ledger durante la sesión.

## Cambios

- El saldo de inicio ahora consulta el Ledger.
- Enviar dinero y pagar con QR comparten la misma instancia de Ledger.
- El historial transforma las entradas DEBIT/CREDIT del Ledger en movimientos.
- Cada movimiento muestra:
  - tipo;
  - monto;
  - moneda;
  - fecha y hora;
  - ID de transacción.

Esto elimina el problema de tener un ledger separado por pantalla.

## Limitación

El Ledger sigue siendo en memoria. Cerrar la aplicación elimina el estado de demostración. La versión productiva deberá mover esta fuente de verdad a backend/PostgreSQL con transacciones ACID, auditoría y reconciliación.
