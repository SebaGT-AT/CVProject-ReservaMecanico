# Etapa 6 — Dashboard operativo

## Alcance entregado

- Dashboard profesional calculado con datos reales.
- Dashboard de cliente con reservas próximas, completadas y siguiente cita.
- Próximas cinco citas del profesional.
- Consultas agregadas en base de datos, sin cargar historiales completos en memoria.
- Cálculos diarios y mensuales según la zona horaria IANA del profesional.
- Capacidad restante que considera semana, excepciones, anticipación, citas y buffers.
- Índice parcial específico para el cálculo de clientes nuevos.

## Definición de métricas profesionales

### Reservas de hoy

Citas cuyo inicio pertenece al día local del profesional y cuyo estado no es `CANCELLED`. Incluye pendientes, confirmadas, completadas y ausencias, por lo que la cifra no disminuye al cerrar una atención.

### Clientes nuevos este mes

Clientes cuya primera cita no cancelada con ese profesional comienza dentro del mes local actual. Se calcula con `GROUP BY customer_id` y `MIN(start_at)` directamente en PostgreSQL.

### Disponible hoy

Minutos restantes dentro del horario efectivo del día:

1. aplica semana habitual y excepciones;
2. elimina el tiempo anterior a la anticipación mínima;
3. resta citas pendientes o confirmadas;
4. incluye el buffer posterior como tiempo ocupado.

Es una métrica de capacidad temporal general, no la cantidad de slots de un servicio particular.

## Endpoint

```http
GET /api/v1/professional/dashboard
Authorization: Bearer {access-token}
```

Respuesta:

```json
{
  "date": "2026-06-22",
  "timeZone": "America/Santiago",
  "appointmentsToday": 4,
  "newCustomersThisMonth": 3,
  "availableMinutesToday": 150,
  "upcomingAppointments": []
}
```

## Zona horaria

Los límites del día y del mes se construyen primero en la zona profesional y luego se convierten a `Instant`. El resultado no depende de la zona del servidor ni de la computadora del usuario.

## Dashboard del cliente

Usa el historial privado ya existente y presenta:

- cantidad de reservas futuras activas;
- cantidad de citas completadas;
- fecha, servicio y profesional de la siguiente cita.

## Rendimiento

- Conteos y clientes nuevos se resuelven en SQL.
- Las próximas citas se limitan a cinco filas.
- La migración V7 agrega `(professional_id, customer_id, start_at)` solo para citas no canceladas.
- El motor de capacidad consulta únicamente el día actual.

## Verificación

- Límites de día y mes para `America/Santiago` comprobados con reloj fijo.
- Capacidad libre comprobada restando una cita existente.
- 34 pruebas backend, build TypeScript y ESLint.
