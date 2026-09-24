# IZIDA — Sistema completo

**Cliente:** billetera IZIDA en PEN, transferencias, cobros/pagos QR preparados, movimientos, créditos en PEN, inversiones PEN/USD y campañas.

**Administrador:** dashboard, clientes, billeteras, créditos, campañas, inversiones, perfil de riesgo e integraciones.

### Monedas
- Billetera: PEN (S/)
- Créditos: PEN (S/)
- Inversiones: PEN (S/) y USD (US$)

### Ejecutar
1. `cd backend`
2. `pip install -r requirements.txt`
3. `uvicorn main:app --reload`
4. Abrir `cliente/index.html` y `admin/index.html`.

API: `http://localhost:8000`
Docs: `http://localhost:8000/docs`

### Producción
Esta entrega es una base funcional. Para operar dinero real se deben añadir autenticación segura, hash de PIN, MFA, KYC/AML, protección de datos, límites, conciliación, auditoría y las integraciones oficiales necesarias. SBS/Sentinel, CrediCorp e InterCorp no llevan credenciales ni endpoints inventados; se conectan cuando existan mecanismos oficiales autorizados.
