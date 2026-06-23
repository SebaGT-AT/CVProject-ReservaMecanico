# Checklist de lanzamiento

## Producto

- [ ] Flujos de cliente y profesional aprobados por negocio.
- [ ] Textos, zonas horarias, precios y políticas de cancelación revisados.
- [ ] Soporte conoce búsqueda de usuarios y bandeja de fallos.

## Seguridad y privacidad

- [ ] Secretos únicos por ambiente y rotación documentada.
- [ ] Administradores nominales; ninguna cuenta compartida.
- [ ] TLS, CORS, cookies y OAuth verificados en el dominio final.
- [ ] Privacidad, términos, retención y proveedores aprobados legalmente.

## Operación

- [ ] CI y release del tag en verde.
- [ ] Backup y restauración probados en staging.
- [ ] Dashboards, alertas y contacto de incidentes activos.
- [ ] Rollback a la imagen anterior ensayado sin revertir migraciones.
- [ ] Smoke test posterior al despliegue documentado.
