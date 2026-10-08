# IZIDA — Paso 6: QR

Se agregó la base del flujo QR:

- Payload IZIDA versionado.
- Generación lógica del identificador de cuenta y titular.
- Validación/decodificación del payload.
- Pantalla "Mi QR".
- Pantalla "Escanear QR".
- Preparación para conectar la cámara y el flujo de pago.

El payload actual es una representación de desarrollo. No debe usarse como mecanismo de autenticación ni como autorización de pago.

Producción debe incorporar:
- QR firmado o payload autenticado;
- expiración cuando corresponda;
- cámara/lector QR;
- validación server-side;
- prevención de replay;
- confirmación explícita del monto y destinatario;
- PIN/biometría antes de autorizar;
- auditoría e idempotencia.
