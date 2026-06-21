# Etapa 5 — Notificaciones confiables

## Alcance entregado

- Transactional outbox persistido en PostgreSQL como `JSONB`.
- Eventos de confirmación y cancelación creados en la misma transacción de la cita.
- Recordatorios automáticos aproximadamente 24 horas antes.
- Deduplicación mediante clave única por cita y tipo de evento.
- Worker por lotes compatible con múltiples instancias mediante `FOR UPDATE SKIP LOCKED`.
- Recuperación de mensajes que quedaron `PROCESSING` por una caída del proceso.
- Reintentos con backoff exponencial y estado terminal `DEAD`.
- Entrega por log en desarrollo y SMTP configurable en producción.
- Correos dirigidos al cliente y profesional según el evento.

## Por qué outbox

Guardar una cita y llamar a SMTP dentro de la misma petición introduce dos fallas posibles:

- si SMTP falla, no debe perderse una reserva válida;
- si la base de datos revierte después del correo, no debe enviarse una confirmación falsa.

La cita y el evento outbox se confirman juntos. El worker procesa el correo después. Así la base de datos sigue siendo la fuente de verdad y la entrega puede reintentarse sin repetir la operación de negocio.

```text
Transacción de cita
  ├── appointments
  ├── appointment_status_history
  └── notification_outbox (PENDING)

Worker
  PENDING/FAILED -> PROCESSING -> SENT
                         └──────> FAILED -> ... -> DEAD
```

## Eventos

| Evento | Destinatarios | Clave de deduplicación |
|---|---|---|
| `APPOINTMENT_CONFIRMED` | Cliente y profesional | `appointment:{id}:confirmed:{recipient}` |
| `APPOINTMENT_CANCELLED` | Cliente y profesional | `appointment:{id}:cancelled:{recipient}` |
| `APPOINTMENT_REMINDER_24H` | Cliente | `appointment:{id}:reminder:24h:customer` |

Cada destinatario tiene su propio evento; si el correo profesional falla después de entregar el del cliente, solo se reintenta el profesional. El payload conserva destinatario, nombres, servicio, instante UTC y zona horaria necesarios para renderizar el mensaje sin reconstruir el evento histórico.

## Concurrencia y recuperación

El worker reclama mensajes con bloqueo de fila y `SKIP LOCKED`; dos réplicas no procesan el mismo lote. La transacción de reclamo es breve y termina antes de contactar SMTP. Un mensaje en `PROCESSING` por más de diez minutos vuelve a ser reclamable.

La inserción usa `ON CONFLICT DO NOTHING` sobre `deduplication_key`. Esto hace seguros tanto los recordatorios ejecutados en varias réplicas como los reintentos del scheduler.

## Reintentos

- Primer reintento: 30 segundos.
- Backoff exponencial hasta un máximo de una hora.
- Máximo predeterminado: 8 intentos.
- Después queda `DEAD` para inspección y futura herramienta administrativa.

## Configuración

```text
MAIL_DELIVERY=log|smtp
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_FROM=no-reply@example.com
NOTIFICATION_POLL_DELAY_MS=5000
NOTIFICATION_BATCH_SIZE=20
NOTIFICATION_MAXIMUM_ATTEMPTS=8
```

Las credenciales SMTP se proporcionan mediante las propiedades estándar `SPRING_MAIL_USERNAME` y `SPRING_MAIL_PASSWORD`.

## Recordatorios

Cada quince minutos se buscan citas confirmadas entre 23 y 25 horas en el futuro. La ventana solapada evita perder citas por pequeñas pausas del proceso; la clave única evita recordatorios duplicados.

## Verificación automatizada

- Serialización del evento y clave determinista.
- Reclamo del lote y transición a `PROCESSING`.
- Backoff después de un fallo.
- Entrega exitosa marcada como `SENT`.
- Error SMTP enviado a reintento.
- Publicación de confirmación y cancelación desde citas.

La migración V6 se validará contra PostgreSQL cuando Docker Desktop esté disponible en el entorno local.
