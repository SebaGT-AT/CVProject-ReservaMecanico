# Etapa 4 — Citas y reservas

## Alcance entregado

- Reserva autenticada para usuarios `CUSTOMER` desde el perfil público.
- Confirmación inmediata de la cita.
- Clave de idempotencia por intento para tolerar reenvíos y doble clic.
- Snapshot de servicio, precio, moneda, duración y zona horaria.
- Buffer profesional incluido en el período ocupado.
- Restricción PostgreSQL que impide físicamente citas activas solapadas.
- Estados `PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED` y `NO_SHOW`.
- Historial inmutable de cada transición con actor, motivo y fecha.
- Cancelación del cliente sujeta a anticipación configurable.
- Gestión profesional para confirmar, completar, marcar ausencia o cancelar.
- Historial privado para cliente y agenda operativa para profesional.
- Los slots públicos descuentan citas pendientes o confirmadas.

## Garantía contra doble reserva

La API vuelve a calcular la disponibilidad dentro de la transacción. Aun así, dos solicitudes pueden observar el mismo slot antes de insertar. La garantía final vive en PostgreSQL:

```sql
EXCLUDE USING gist (
  professional_id WITH =,
  tstzrange(start_at, busy_until, '[)') WITH &&
) WHERE (status IN ('PENDING', 'CONFIRMED'))
```

El rango semiabierto permite que una cita comience exactamente cuando termina el período ocupado anterior. `busy_until` incluye el buffer posterior. Cancelar una cita libera el horario porque la restricción solo considera estados activos.

La migración habilita `btree_gist`, extensión estándar necesaria para combinar igualdad por profesional y solapamiento temporal.

## Idempotencia

`POST /api/v1/appointments` recibe un UUID `idempotencyKey`. La combinación cliente/clave es única. Repetir la misma solicitud devuelve la cita existente en lugar de crear otra.

## Endpoints de cliente

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/appointments` | Reservar slot disponible |
| GET | `/api/v1/appointments/mine` | Historial propio |
| POST | `/api/v1/appointments/{id}/cancel` | Cancelar cita propia |

Ejemplo de reserva:

```json
{
  "professionalSlug": "ada",
  "serviceId": "00000000-0000-0000-0000-000000000000",
  "startAt": "2026-06-22T13:00:00Z",
  "idempotencyKey": "00000000-0000-0000-0000-000000000001"
}
```

## Endpoints profesionales

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/professional/appointments?from=&to=` | Citas del rango |
| PATCH | `/api/v1/professional/appointments/{id}/status` | Transición de estado |

Transiciones permitidas:

```text
PENDING   -> CONFIRMED | CANCELLED
CONFIRMED -> COMPLETED | NO_SHOW | CANCELLED
```

Los estados terminales no admiten nuevas transiciones. `COMPLETED` y `NO_SHOW` solo pueden registrarse después del inicio.

## Política de cancelación

`cancellationNoticeMinutes` define hasta cuándo el cliente puede cancelar en línea. El profesional conserva la capacidad de cancelar operativamente. Toda cancelación guarda actor, momento y motivo.

## Verificación

- Reserva de un slot válido y creación de historial.
- Repetición idempotente sin segunda escritura.
- Rechazo de slot que dejó de estar disponible.
- Cancelación del cliente y transición profesional.
- Eliminación de slots ocupados del motor público.
- 26 pruebas backend, build TypeScript y ESLint.

Docker Desktop no estaba operativo durante el cierre local, por lo que la migración PostgreSQL debe validarse al iniciar el entorno con `docker compose up -d`. Las reglas de dominio y consultas están cubiertas por pruebas automatizadas; la exclusión requiere PostgreSQL real.
