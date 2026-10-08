# Paso 2 — Seguridad y usuarios

## Incluido
- Flujo de registro → identidad → creación de PIN.
- Validación local de formato de celular, DNI y PIN.
- Sesión de aplicación con ciclo iniciar/bloquear.
- Centro de seguridad.
- Clave AES protegida por Android Keystore para secretos locales futuros.
- Hash PBKDF2 para PIN cuando se integre almacenamiento seguro.

## Importante
Esta implementación todavía no autentica contra un servidor. No debe usarse para custodiar dinero real. En la siguiente etapa se conectará el backend con sesiones revocables, límites, auditoría, recuperación y validación de identidad.
