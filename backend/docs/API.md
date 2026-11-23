# API Junta Zaror — referencia para la app

**Base URL (Retrofit):** `http://10.0.2.2:8080/api/` desde el emulador · `http://localhost:8080/api/` desde el PC.
Header en todo lo que no sea público: `Authorization: Bearer <token>` (expira en 8 h → si responde 401, volver a login).

Formatos: fechas `"yyyy-MM-dd"` · horas `"HH:mm"` · montos en CLP como enteros · enums en MAYÚSCULAS.
Errores (400/401/403/404/409): `{"status":409,"error":"Conflict","mensaje":"...","timestamp":"..."}` → mostrar `mensaje`.

Roles: **V** = VECINO · **D** = DIRECTIVA · **T** = TESORERO · **Todos** = cualquier usuario autenticado.

---

## Auth (público)

### `POST /auth/login`
```json
{ "email": "camila@zaror.cl", "password": "1234" }
```
**200**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "id": 3, "nombre": "Camila Rojas", "email": "camila@zaror.cl", "rol": "VECINO" }
```
401 credenciales incorrectas · 400 campos vacíos/email inválido.

### `POST /auth/register`
```json
{ "nombre": "Ana Torres", "email": "ana@zaror.cl", "telefono": "+56955555555", "numeroCasa": "30",
  "fechaNacimiento": "1990-04-12", "password": "1234", "recibirAvisos": true }
```
**200** igual que login (`rol` siempre `"VECINO"`). 409 email ya registrado. `recibirAvisos` es obligatorio.

---

## Usuarios (Todos)

### `GET /usuarios/me`
**200**
```json
{ "id": 3, "nombre": "Camila Rojas", "email": "camila@zaror.cl", "telefono": "+56933333333",
  "numeroCasa": "45", "fechaNacimiento": "1995-11-02", "recibirAvisos": true }
```

### `PUT /usuarios/me`
```json
{ "nombre": "Camila Rojas", "telefono": "+56933333333", "numeroCasa": "45", "recibirAvisos": true }
```
**200** mismo formato que `GET /usuarios/me`.

---

## Sedes

### `GET /sedes` (Todos)
**200**
```json
[ { "id": 1, "nombre": "Salón principal", "descripcion": "Salón amplio con escenario...",
    "ubicacion": "Sede social, primer piso", "capacidad": 80, "precio": 35000, "activa": true,
    "fotos": ["/api/sedes/fotos/salon-1.jpg", "/api/sedes/fotos/salon-2.jpg"] } ]
```
Las fotos son rutas: URL completa = `http://10.0.2.2:8080` + ruta (públicas, sin token).

### `GET /sedes/{id}/disponibilidad?anio=2026&mes=11` (Todos)
**200** (horarios ocupados PENDIENTE/APROBADA; sin datos personales)
```json
[ { "fecha": "2026-11-28", "horaInicio": "15:00", "horaFin": "20:00", "estado": "APROBADA" } ]
```
404 sede no existe · 400 mes fuera de 1–12.

### `GET /sedes/fotos/{archivo}` (público) → imagen JPG.

---

## Reservas

**Objeto reserva** (respuesta de todos los endpoints de reservas):
```json
{ "id": 2, "sedeId": 1, "sedeNombre": "Salón principal", "sedeFoto": "/api/sedes/fotos/salon-1.jpg",
  "usuarioId": 3, "usuarioNombre": "Camila Rojas", "fecha": "2026-11-28", "horaInicio": "15:00",
  "horaFin": "20:00", "motivo": "Cumpleaños de mi hija", "asistentes": 50, "estado": "APROBADA",
  "pagada": false, "precio": 35000, "observacion": "Aprobada, recuerde dejar el salón limpio" }
```
`estado`: `PENDIENTE | APROBADA | RECHAZADA | CANCELADA`.

### `POST /reservas` (Todos) → **201** reserva (`estado: "PENDIENTE"`)
```json
{ "sedeId": 2, "fecha": "2026-12-12", "horaInicio": "10:00", "horaFin": "13:00",
  "motivo": "Taller de repostería", "asistentes": 10 }
```
400: fecha pasada / hora fin ≤ inicio / asistentes fuera de 1..capacidad / sede inactiva.
409: `"La sede ya está reservada en ese horario"`.

### `GET /reservas/mias` (Todos) → **200** lista de reservas del usuario del token.

### `GET /reservas?estado=PENDIENTE` (D, T) → **200** lista (sin `estado` = todas).

### `PUT /reservas/{id}/estado` (D) → **200** reserva
```json
{ "estado": "APROBADA", "observacion": "Aprobada, dejar el espacio limpio" }
```
`estado` solo `APROBADA` o `RECHAZADA` (400). 409 si no está PENDIENTE o si al aprobar se cruza con otra aprobada.

### `DELETE /reservas/{id}` (dueño) → **200** reserva con `estado: "CANCELADA"`
403 no es el dueño · 409 pagada o ya cancelada/rechazada.

---

## Finanzas

### `GET /finanzas/resumen?anio=2026&mes=11` (Todos)
**200** (`saldo` = acumulado al cierre del mes)
```json
{ "saldo": 376800, "ingresosMes": 93000, "gastosMes": 93000,
  "gastosPorCategoria": [ { "categoria": "Luz", "total": 41200 }, { "categoria": "Gas", "total": 32000 } ] }
```

### `GET /categorias` (T)
**200**
```json
[ { "id": 1, "nombre": "Arriendo sede", "tipo": "INGRESO" }, { "id": 5, "nombre": "Luz", "tipo": "GASTO" } ]
```
INGRESO: 1 Arriendo sede, 2 Cuotas, 3 Donación, 4 Otro ingreso · GASTO: 5 Luz, 6 Agua, 7 Gas, 8 Mantención, 9 Materiales, 10 Otro gasto.

**Objeto movimiento:**
```json
{ "id": 9, "tipo": "INGRESO", "categoriaId": 2, "categoriaNombre": "Cuotas", "reservaId": null,
  "descripcion": "Cuotas sociales noviembre", "monto": 78000, "fechaMovimiento": "2026-11-05",
  "estado": "VIGENTE", "usuarioRegistroId": 2, "tieneComprobante": false }
```

### `GET /movimientos?anio=2026&mes=11` (D, T) → **200** lista (incluye `ANULADO`).

### `POST /movimientos` (T) — **multipart/form-data** → **201** movimiento
- `datos` (Content-Type `application/json`):
  ```json
  { "tipo": "GASTO", "categoriaId": 9, "descripcion": "Ampolletas para el salón", "monto": 12990, "fechaMovimiento": "2026-11-16" }
  ```
- `archivo` (opcional): JPG/PNG, máx. 5 MB.

400: categoría de otro tipo, monto ≤ 0, archivo no JPG/PNG o > 5 MB.

Retrofit:
```kotlin
@Multipart @POST("movimientos")
suspend fun crearMovimiento(@Part("datos") datos: RequestBody, @Part archivo: MultipartBody.Part?): MovimientoDto
// datos = json.toRequestBody("application/json".toMediaType())
// archivo = MultipartBody.Part.createFormData("archivo", "boleta.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
```

### `PUT /movimientos/{id}` (T) → **200** movimiento. Body JSON igual a `datos`. 409 si está ANULADO.

### `DELETE /movimientos/{id}` (T) → **200** movimiento con `estado: "ANULADO"`. 409 si ya estaba anulado.

### `GET /movimientos/{id}/comprobante` (D, T) → **200** imagen (`image/jpeg` o `image/png`). 404 si no tiene.

---

## Pagos

**Objeto pago:**
```json
{ "id": 1, "reservaId": 1, "usuarioNombre": "Camila Rojas", "sedeNombre": "Cocina comunitaria",
  "fechaReserva": "2026-11-10", "monto": 15000, "fechaPago": "2026-11-10", "medio": "EFECTIVO",
  "referencia": "Recibo N° 0001", "estado": "PAGADO", "movimientoId": 11 }
```
`medio`: `WEBPAY | TRANSFERENCIA | EFECTIVO | OTRO` · `estado`: `PENDIENTE | PAGADO | RECHAZADO`.

### `GET /pagos` (D, T) → **200** lista.

### `POST /pagos` (T) — **multipart/form-data** → **201** pago (`PAGADO`)
- `datos` (`application/json`):
  ```json
  { "reservaId": 3, "monto": 25000, "medio": "TRANSFERENCIA", "fechaPago": "2026-11-18", "referencia": "TRF-998877" }
  ```
- `archivo` (**obligatorio**): JPG/PNG, máx. 5 MB.

400: `medio` WEBPAY o falta archivo · 409: reserva no APROBADA o ya pagada.
Efecto: crea el ingreso "Arriendo sede" y marca la reserva `pagada: true`.

### `POST /pagos/webpay/iniciar` (V, dueño de la reserva)
```json
{ "reservaId": 2 }
```
**200**
```json
{ "url": "https://webpay3gint.transbank.cl/webpayserver/initTransaction", "token": "01ab6d9aad32..." }
```
Abrir en el navegador/Custom Tab: `url + "?token_ws=" + token`. El monto lo define el servidor.
403 no es el dueño (o no es VECINO) · 409 reserva no APROBADA o ya pagada.

### `GET|POST /pagos/webpay/retorno` (público, lo llama Transbank) → **200** HTML
Página "Pago aprobado/rechazado" que redirige a `zaror://pago?estado=ok` o `zaror://pago?estado=error`.
La app, al recibir el deep link, recarga `GET /reservas/mias`.

---

## Informes

### `GET /informes` (Todos; VECINO solo publicados)
**200**
```json
[ { "id": 1, "titulo": "Rendición octubre", "periodoInicio": "2026-10-01", "periodoFin": "2026-10-31",
    "observacion": "Rendición de cuentas presentada en la asamblea de noviembre.", "publicado": true,
    "fechaGeneracion": "2026-11-03T20:00:00" } ]
```

### `GET /informes/{id}` (Todos) → **200** (404 si el vecino pide uno no publicado)
```json
{ "id": 1, "titulo": "Rendición octubre", "periodoInicio": "2026-10-01", "periodoFin": "2026-10-31",
  "observacion": "...", "publicado": true, "fechaGeneracion": "2026-11-03T20:00:00",
  "totalIngresos": 500300, "totalGastos": 123500, "saldoPeriodo": 376800,
  "gastosPorCategoria": [ { "categoria": "Mantención", "total": 45000 }, { "categoria": "Luz", "total": 38450 } ] }
```

### `POST /informes` (T) → **201** igual a `GET /informes/{id}`
```json
{ "titulo": "Rendición noviembre", "periodoInicio": "2026-11-01", "periodoFin": "2026-11-30",
  "observacion": "Borrador", "publicado": false }
```
400 si `periodoFin` < `periodoInicio`.
