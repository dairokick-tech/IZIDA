# IZIDA — Paso 8: comprobantes e historial de operaciones

Se agregó:

- Modelo PaymentReceipt.
- Almacenamiento de comprobantes en memoria para desarrollo.
- Pantalla de comprobante.
- Datos de operación, destinatario, monto, moneda, estado, fecha, ID y referencia.

La persistencia definitiva debe realizarse en backend/PostgreSQL.

La cámara QR real queda como integración de infraestructura del siguiente bloque para no convertir el prototipo actual en una falsa autorización financiera: leer un QR no autoriza por sí solo un pago. El backend debe validar destinatario, importe, sesión, límites e idempotencia antes de registrar la operación.
