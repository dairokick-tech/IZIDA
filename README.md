# IZIDA — Billetera digital independiente

## Acceso administrador
Usuario: `admin`
Contraseña: `IZIDA2026`

## Ejecutar
Windows: doble clic en `INICIAR_WINDOWS.bat`.
Manual:
```bash
cd backend
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

## URLs
Cliente: http://127.0.0.1:8000/cliente
Administrador: http://127.0.0.1:8000/admin
API: http://127.0.0.1:8000/docs

## Flujo conectado
Cliente se registra -> obtiene billetera PEN -> abono/retiro interno -> envía/paga a otro usuario IZIDA -> movimientos y referencias quedan registrados -> administrador ve clientes, billeteras y movimientos.
Administrador crea crédito -> calcula total/cuota -> aprueba -> desembolsa a la billetera -> cliente paga desde su saldo -> saldo pendiente se actualiza.
Administrador crea campañas -> cliente elegible por saldo las ve.
Cliente crea inversiones PEN/USD -> administrador las ve.
Auditoría registra acciones.

No existen integraciones externas en esta versión. No usa Yape, Plin, bancos, SBS, Sentinel ni APIs de terceros. Las operaciones de abono/retiro son internas del sistema y no representan movimiento bancario real.
