# Fotos de las sedes

Imágenes que sirve ms-reserva. Las rutas están en la base de datos (migración `V3__actualizar_fotos_sedes.sql`).
La primera foto de cada sede es la **portada** y es la que aparece como `sedeFoto` en las reservas.

| Archivo        | Sede               | Contenido |
|----------------|--------------------|-----------|
| `salon-1.jpg`  | Salón principal    | Interior con escenario (portada) |
| `salon-2.jpg`  | Salón principal    | Fachada de la sede |
| `cocina-1.jpg` | Cocina comunitaria | Cocina equipada (portada) |
| `patio-1.jpg`  | Patio y quincho    | Patio con quincho y parrilla (portada) |

Se sirven públicamente (sin token):

- Vía gateway: `http://localhost:8080/api/sedes/fotos/salon-1.jpg`
- Desde el emulador Android: `http://10.0.2.2:8080/api/sedes/fotos/salon-1.jpg`
- Directo (solo IDE): `http://localhost:8083/sedes/fotos/salon-1.jpg`

Formato: JPG, ~1600 px de ancho, menos de 500 KB cada una.
Las fotos se empaquetan dentro del .jar: después de agregar o cambiar una, reinicia ms-reserva
(en Docker: `docker compose up -d --build ms-reserva`). Para cambiar qué fotos tiene cada sede,
crea una nueva migración Flyway (no edites las ya aplicadas).
