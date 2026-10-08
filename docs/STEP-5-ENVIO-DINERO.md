# IZIDA — Paso 5: Enviar dinero

Se implementó el flujo inicial de envío:

1. Ingresar número celular de 9 dígitos.
2. Buscar destinatario mediante un directorio.
3. Mostrar destinatario encontrado.
4. Ingresar monto en PEN.
5. Revisar la confirmación.
6. Crear una transacción mediante TransactionService.
7. Mostrar comprobante básico con ID de operación.

## Seguridad y límites actuales

El directorio y ledger usados en esta etapa son de demostración y viven en memoria. No representan una red de pagos real.

Antes de producción se debe agregar:
- autenticación backend real;
- PIN/biometría validada por servidor;
- límites por operación y por día;
- antifraude;
- persistencia PostgreSQL;
- auditoría;
- reversos;
- notificaciones;
- comprobante persistente;
- API de directorio/alias autorizada.

No se debe afirmar interoperabilidad con Yape, Plin u otra red hasta disponer de una integración autorizada.
