# IZIDA

**Tu dinero. Tu control.**

Base de aplicación Android nativa para IZIDA, una billetera digital diseñada para crecer hacia una arquitectura financiera real.

## Paso 1
- Kotlin + Jetpack Compose.
- Navegación inicial: bienvenida, acceso, registro y principal.
- Separación inicial por features, dominio y datos.
- Sin secretos ni claves API en el repositorio.
- La autenticación y el saldo de esta etapa son de interfaz/prototipo; no representan operaciones financieras reales.

## Arquitectura prevista
El backend y el ledger serán la fuente de verdad del saldo. Las operaciones financieras futuras deberán usar estados transaccionales, idempotencia, auditoría y conciliación.

## Próximos pasos
1. Seguridad, usuarios y sesiones.
2. Cuenta IZIDA y movimientos.
3. Ledger transaccional.
4. Envío de dinero.
5. QR.
6. Administración, auditoría y conciliación.
7. Integraciones autorizadas.

## Requisitos
Android Studio reciente, JDK 17 y Android SDK API 35.
