# Etapa 2 — Profesionales y servicios

## Alcance entregado

- Perfil profesional único por usuario.
- URL pública (`slug`) única y estable.
- Biografía, teléfono y zona horaria IANA.
- Catálogo inicial de especialidades con relación muchos-a-muchos.
- Servicios con nombre, descripción, duración, precio, moneda y estado.
- Borrado lógico de servicios para preservar el futuro historial de citas.
- Perfil público que solo expone profesionales publicados y servicios activos.
- Autorización por rol y propiedad para todas las operaciones privadas.
- Bloqueo de publicación sin especialidad y sin servicios activos.
- Bloqueo optimista para evitar sobrescrituras concurrentes.

## Endpoints

### Públicos

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/specialties` | Especialidades activas |
| GET | `/api/v1/professionals/{slug}` | Perfil y servicios publicados |

### Profesional autenticado

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/professional/profile` | Perfil propio |
| PUT | `/api/v1/professional/profile` | Crear o actualizar perfil |
| GET | `/api/v1/professional/services` | Servicios propios, incluidos inactivos |
| POST | `/api/v1/professional/services` | Crear servicio |
| PUT | `/api/v1/professional/services/{id}` | Actualizar servicio propio |
| DELETE | `/api/v1/professional/services/{id}` | Desactivar servicio propio |

## Decisiones de dominio

- Las especialidades son catálogo, no texto libre. Esto permite búsquedas y filtros consistentes.
- Un profesional puede tener hasta cinco especialidades.
- La zona horaria se valida con `ZoneId`; los horarios de la siguiente etapa se interpretarán en esa zona.
- La moneda usa código ISO 4217 y el importe se persiste como `NUMERIC(12,2)`, nunca `double`.
- Si se desactiva el último servicio activo, el perfil se despublica automáticamente.
- Los servicios se desactivan y no se eliminan físicamente porque las citas futuras conservarán su referencia.

## Flujo de interfaz

1. Un usuario `PROFESSIONAL` abre `/perfil-profesional`.
2. Guarda su perfil como borrador.
3. Agrega uno o más servicios.
4. Activa `Publicar perfil`.
5. Revisa la vista pública en `/p/{slug}`.

## Verificación

- `mvn test`: pruebas de creación, slug duplicado, reglas de publicación y propiedad.
- `npm run build`: compilación TypeScript y bundle de producción.
- `npm run lint`: análisis estático sin errores.
- La migración `V3` se ejecuta automáticamente con Flyway al iniciar contra PostgreSQL.
