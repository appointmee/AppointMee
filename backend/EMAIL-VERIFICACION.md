# Verificación de email en el backend

## Activación en Supabase

Ejecutar `src/main/resources/db/email_verificacion.sql` después de `auth_tokens.sql`, antes de iniciar esta versión. Mantiene `ddl-auto=validate`. El script agrega `usuarios.email_verificado` y vincula los códigos de la tabla `verificacion` a un `usuario_id`. Si esa tabla no existe, la crea. No elimina datos.

La primera ejecución deja las cuentas existentes pendientes de verificación, igual que las nuevas. Repetir el script no vuelve a poner en pendiente una cuenta ya verificada. Los códigos genéricos antiguos, sin usuario asociado, no pueden verificar cuentas. Las tablas requieren acceso desde la conexión PostgreSQL del backend; se bloquea su acceso público por roles anon/authenticated.

Configurar `RESEND_API_KEY` y `RESEND_FROM` para enviar correos. No se usa Supabase Auth ni se ejecuta la migración automáticamente. La aplicación usa el proveedor Resend existente.

## Contrato para Angular

- `POST /auth/register` y su alias `/usuarios/crear`: crean un CLIENTE activo, con email sin verificar, e intentan enviar el código automáticamente. Respuesta 201.
- `POST /auth/login` y su alias `/usuarios/inicio-sesion`: contraseña incorrecta, cuenta inexistente o desactivada reciben 401 genérico. Solo con contraseña correcta y email pendiente se responde **403** con `{"code":"EMAIL_NOT_VERIFIED","message":"Verificá tu email para iniciar sesión"}`. No se emite ningún token. Esto activa el modal de Angular.
- `POST /auth/send-verification`: cuerpo `{"email":"usuario@example.com"}`. Público, sin Bearer. Responde **202** con mensaje genérico para cuentas pendientes, verificadas, inexistentes, cooldown y fallos HTTP del proveedor. Un 202 no garantiza entrega de correo.
- `POST /auth/verify-email`: cuerpo `{"email":"usuario@example.com","codigo":"123456"}`. Público, sin Bearer. Éxito **200**: marca email verificado y consume el código; el usuario puede iniciar sesión. Código inválido/vencido/usado o intentos agotados: **400** genérico.

Los emails se normalizan con trim y minúsculas. No se aceptan códigos de otro usuario, de otro email o del flujo genérico previo. No se marca una cuenta como verificada al restablecer la contraseña.

## Códigos y sesiones

Código aleatorio de 6 dígitos, hash BCrypt en la base, 5 minutos de vigencia, máximo 5 intentos incorrectos persistidos. Mínimo 60 segundos entre envíos; reenvío invalida el código anterior. Los bloqueos por usuario serializan las operaciones concurrentes. Solo se muestra el estado sin verificar después de validar credenciales; los mensajes de envío no revelan existencia de cuenta, sin promesa de tiempo de respuesta constante.

JWT y refresh verifican también que el email esté confirmado. Cambiar email vuelve a dejar la cuenta pendiente y revoca sesiones y códigos anteriores. La baja también invalida códigos. `activo` y `emailVerificado` son estados distintos.

## Probar

Importar la colección actualizada en `postman/Appointme-Auth.postman_collection.json`.

1. Registrar una cuenta nueva con email real, o usar una cuenta pendiente después de la migración.
2. Login con contraseña incorrecta: 401 sin código de verificación.
3. Login con contraseña correcta: 403 EMAIL_NOT_VERIFIED (Angular abre el modal).
4. Solicitar/revisar el código del correo y ponerlo en `verificationCode` en Postman.
5. Confirmar email: 200. Reutilizar el mismo código: 400.
6. Login nuevamente: 200 con access y refresh.

La pantalla de Angular `/configuracion/verificar-email` todavía es provisional. Este cambio implementa el backend y el disparador real del modal; falta conectar esa pantalla con envío y confirmación de código. No se modificó el frontend en esta tarea.

Las pruebas usan H2 y un EmailService simulado; no tocan Supabase ni envían correos reales. Incluyen registro, login seguro, confirmación pública, vencimiento, cinco intentos, reenvío, aislamiento frente a códigos genéricos y revocación al cambiar email.
