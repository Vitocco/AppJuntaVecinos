# Junta Zaror — Backend de microservicios

Backend de la app Android de la **Junta de Vecinos Población Zaror** (DUOC UC · DSY1105).
Permite **reservar las sedes comunitarias** y da **transparencia financiera** a los vecinos
(movimientos, comprobantes, pagos de arriendo e informes de rendición).

Java 21 · Spring Boot 4.1.2 · Spring Cloud 2025.1.4 (Eureka, Gateway, OpenFeign) · MySQL 8 + Flyway · JWT (jjwt) · Webpay Plus (Transbank, integración)

> **Webpay funciona en el ambiente de INTEGRACIÓN de Transbank: no se realizan transacciones reales ni se mueve dinero.**

---

## 1. Arquitectura

```
                       App Android (Retrofit)
                     http://10.0.2.2:8080/api/...
                                 │
                                 ▼
                    ┌─────────────────────────┐        ┌──────────────────┐
                    │  ms-gateway  :8080      │◄──────►│  ms-eureka :8761 │
                    │  - valida JWT           │ lb://  │  (registro de    │
                    │  - bloquea /interno     │        │   servicios)     │
                    └───────────┬─────────────┘        └──────────────────┘
          ┌──────────────┬──────┴───────┬──────────────────┐   ▲ todos se registran
          ▼              ▼              ▼                  ▼   │
   ┌────────────┐ ┌────────────┐ ┌──────────────┐  ┌──────────────┐
   │ ms-auth    │ │ ms-usuario │ │ ms-reserva   │  │ ms-finanza   │──► Webpay Plus
   │ :8081      │ │ :8082      │ │ :8083        │  │ :8084        │    (Transbank)
   │ login/JWT  │ │ perfil     │ │ sedes/fotos/ │  │ movimientos/ │
   │            │ │            │ │ reservas     │  │ pagos/inform.│
   └─────┬──────┘ └─────▲──────┘ └──────▲───────┘  └──────┬───────┘
         │  Feign       │               │      Feign      │
         └──────────────┘               └─────────────────┘
         (X-Internal-Key)                 (X-Internal-Key)
         │              │               │                 │
         ▼              ▼               ▼                 ▼
     db_auth       db_usuario       db_reserva        db_finanza     ← un MySQL 8, una base por ms
```

| Proyecto   | Puerto | Responsabilidad |
|------------|--------|-----------------|
| ms-eureka  | 8761 | Servidor Eureka |
| ms-gateway | 8080 | Entrada única; valida JWT; enruta con `lb://` (`StripPrefix=1`) |
| ms-auth    | 8081 | Credenciales, login, registro; **único que emite JWT** |
| ms-usuario | 8082 | Perfil del usuario |
| ms-reserva | 8083 | Sedes con fotos y reservas |
| ms-finanza | 8084 | Movimientos, comprobantes, pagos (manual + Webpay), informes |

**Seguridad en 3 capas**
1. **Gateway**: exige `Authorization: Bearer <token>` válido (salvo rutas públicas) y responde **403** a cualquier ruta que contenga `/interno`.
2. **Cada microservicio** vuelve a validar el JWT (`JwtFilter`) y aplica roles con `@PreAuthorize` (`ROLE_VECINO`, `ROLE_DIRECTIVA`, `ROLE_TESORERO`). El id del usuario **siempre sale del token**, nunca del body.
3. **Llamadas internas** (Feign) a `/interno/**` usan el header `X-Internal-Key` en vez de JWT.

Rutas públicas (sin token): `POST /api/auth/login`, `POST /api/auth/register`, `GET|POST /api/pagos/webpay/retorno`, `GET /api/sedes/fotos/**`.

---

## 2. Requisitos

- **Docker Desktop** (para MySQL o para levantar todo).
- Para correr desde el IDE: **JDK 21 o superior** y **VS Code** con *Extension Pack for Java* + *Spring Boot Extension Pack* (o IntelliJ).
- Git Bash (Windows) para el script de pruebas. Postman (opcional).

---

## 3. Configurar el `.env` (obligatorio, una vez por PC)

Los secretos **no están escritos en el código**: `application.yaml` y `docker-compose.yml` solo tienen `${VARIABLE}`.
Los valores reales van en un archivo **`.env`** en la raíz, que **no se sube a git** (está en `.gitignore`).

1. Copia la plantilla:
   ```bash
   # Git Bash / Linux / macOS
   cp .env.example .env
   ```
   ```powershell
   # PowerShell
   Copy-Item .env.example .env
   ```
2. Abre `.env` y reemplaza cada valor `cambia-...` por uno propio. Para generar valores aleatorios:
   ```bash
   openssl rand -hex 32     # en Git Bash; úsalo para JWT_SECRET, INTERNAL_KEY y las contraseñas
   ```
3. Deja tal cual `WEBPAY_COMMERCE_CODE` y `WEBPAY_API_KEY`: son las credenciales **públicas de integración** de Transbank.

| Variable | Para qué sirve |
|----------|----------------|
| `JWT_SECRET` | Firma y valida los tokens (HS256). **Mínimo 32 caracteres.** |
| `INTERNAL_KEY` | Clave del header `X-Internal-Key` entre microservicios |
| `DB_USER` / `DB_PASSWORD` | Usuario de la app en MySQL |
| `DB_ROOT_PASSWORD` | Contraseña root del contenedor MySQL |
| `WEBPAY_COMMERCE_CODE` / `WEBPAY_API_KEY` | Credenciales de integración de Webpay Plus |
| `WEBPAY_RETURN_URL` | A dónde vuelve el navegador después de pagar (ver sección 7) |

> **Importante: todos los microservicios deben usar el MISMO `JWT_SECRET` y la MISMA `INTERNAL_KEY`.**
> ms-auth firma el token con `JWT_SECRET` y el gateway y cada ms lo verifican con ese mismo valor: si uno es distinto,
> todas las peticiones a ese ms responden **401 "Token inválido o expirado"**. Con `INTERNAL_KEY` pasa lo mismo en las
> llamadas Feign (el login responde 503). Por eso hay **un solo `.env`** en la raíz que leen Docker y VS Code.
>
> Cada integrante del equipo puede tener sus propios valores en su `.env`; lo que importa es que, **dentro de un mismo PC**,
> todos los ms lean el mismo archivo.

> Si cambias `DB_USER`/`DB_PASSWORD`/`DB_ROOT_PASSWORD` **después** de haber creado MySQL, el contenedor sigue con las
> contraseñas viejas. Recrea la base (borra los datos, se vuelven a cargar con el seed): `docker compose down -v`.

---

## 4. Levantar el backend

### Opción A — Todo en Docker (recomendada para probar con la app)

```bash
docker compose up -d --build      # la primera vez tarda unos minutos (descarga dependencias)
docker compose ps                 # estado de los contenedores
```

Orden automático: **MySQL (healthy) → Eureka (healthy) → auth, usuario, reserva, finanza → gateway**.
Al host solo se exponen **8080** (gateway), **8761** (Eureka) y **3306** (MySQL).

**Revisar que todo esté registrado:** abre <http://localhost:8761>. En *Instances currently registered with Eureka*
deben aparecer **MS-AUTH, MS-USUARIO, MS-RESERVA, MS-FINANZA y MS-GATEWAY**.
También por consola: `curl -H "Accept: application/json" http://localhost:8761/eureka/apps`.

> Espera **30–60 s** después de que todo arranque antes de la primera petición: es lo que tarda el gateway en descubrir los
> servicios. Antes responde **503**.

```bash
docker compose logs -f ms-finanza   # ver logs de un servicio
docker compose down                 # detener (los datos se conservan)
docker compose down -v              # detener y BORRAR datos (MySQL y comprobantes)
```

### Opción B — Desde VS Code (para desarrollar)

1. Solo MySQL en Docker: `docker compose up -d mysql`
2. En VS Code: **Run and Debug** (`Ctrl+Shift+D`) → elige **"Backend completo (IDE)"** y ▶.
   También puedes lanzar cada uno (`ms-eureka`, `ms-auth`, …) o usar el **Spring Boot Dashboard**.
   Orden recomendado si los lanzas por separado: `ms-eureka` → `ms-auth`, `ms-usuario`, `ms-reserva`, `ms-finanza` → `ms-gateway`.
3. Revisa Eureka en <http://localhost:8761>.

Las configuraciones de [.vscode/launch.json](.vscode/launch.json) cargan el `.env` con `"envFile"`.
`./mvnw spring-boot:run` en una terminal **no** lee el `.env` (fallaría por falta de secretos): usa Run and Debug.

> No uses la Opción A y la B al mismo tiempo: chocan en los puertos 8080 y 8761 (`docker compose down` antes).

---

## 5. Usuarios y datos de prueba

Todos con contraseña **`1234`**:

| Id | Nombre | Email | Rol |
|----|--------|-------|-----|
| 1 | Pedro Soto | directiva@zaror.cl | DIRECTIVA |
| 2 | Rosa Muñoz | tesorero@zaror.cl | TESORERO |
| 3 | Camila Rojas | camila@zaror.cl | VECINO |
| 4 | Luis Pérez | luis@zaror.cl | VECINO |

El registro público (`/api/auth/register`) **siempre crea VECINO**.

**Sedes y fotos** (en `ms-reserva/src/main/resources/static/fotos/`, públicas en `/api/sedes/fotos/{archivo}`):

| Id | Sede | Capacidad | Precio | Fotos (la primera es la portada / `sedeFoto`) |
|----|------|-----------|--------|-----------------------------------------------|
| 1 | Salón principal | 80 | $35.000 | `salon-1.jpg` (interior), `salon-2.jpg` (fachada) |
| 2 | Cocina comunitaria | 15 | $15.000 | `cocina-1.jpg` |
| 3 | Patio y quincho | 40 | $25.000 | `patio-1.jpg` |

**Reservas de noviembre 2026:**

| # | Vecino | Sede | Fecha | Estado | Para probar |
|---|--------|------|-------|--------|-------------|
| 1 | Camila | Cocina | 10-nov 10:00–14:00 | APROBADA, **pagada** (efectivo) | No se puede cancelar ni volver a pagar |
| 2 | Camila | Salón | 28-nov 15:00–20:00 | APROBADA, **sin pagar** | **Pago con Webpay** |
| 3 | Luis | Patio | 22-nov 13:00–18:00 | PENDIENTE | Aprobar/rechazar (directiva) y pago manual |
| 4 | Luis | Salón | 28-nov 18:00–22:00 | RECHAZADA | Se cruzaba con la #2 |

**Finanzas:** movimientos de octubre y noviembre (incluye "Saldo inicial" $331.300 y uno ANULADO de ejemplo) y el
informe publicado **"Rendición octubre"**: ingresos $500.300 − gastos $123.500 = **$376.800**.

---

## 6. Endpoints (vía gateway: `http://localhost:8080`)

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| POST | `/api/auth/register` | Público | Registro de vecino (siempre VECINO); responde igual que login |
| POST | `/api/auth/login` | Público | Login → `{token, id, nombre, email, rol}` |
| GET | `/api/usuarios/me` | Autenticado | Mi perfil |
| PUT | `/api/usuarios/me` | Autenticado | Editar nombre, teléfono, n° de casa y avisos |
| GET | `/api/sedes` | Autenticado | Sedes activas con fotos y precio |
| GET | `/api/sedes/{id}/disponibilidad?anio=&mes=` | Autenticado | Horarios ocupados (PENDIENTE/APROBADA), sin datos personales |
| GET | `/api/sedes/fotos/{archivo}` | Público | Imagen de una sede |
| POST | `/api/reservas` | Autenticado | Crear reserva (queda PENDIENTE) |
| GET | `/api/reservas/mias` | Autenticado | Mis reservas |
| GET | `/api/reservas?estado=` | DIRECTIVA, TESORERO | Todas las reservas (filtro opcional por estado) |
| PUT | `/api/reservas/{id}/estado` | DIRECTIVA | Aprobar o rechazar una reserva PENDIENTE |
| DELETE | `/api/reservas/{id}` | Dueño | Cancelar (pasa a CANCELADA; no si está pagada) |
| GET | `/api/finanzas/resumen?anio=&mes=` | Autenticado | Saldo, ingresos y gastos del mes, gastos por categoría |
| GET | `/api/categorias` | TESORERO | Categorías de movimientos |
| GET | `/api/movimientos?anio=&mes=` | DIRECTIVA, TESORERO | Movimientos del mes (incluye anulados) |
| POST | `/api/movimientos` | TESORERO | Nuevo movimiento — multipart `datos` (JSON) + `archivo` opcional |
| PUT | `/api/movimientos/{id}` | TESORERO | Editar movimiento (JSON) |
| DELETE | `/api/movimientos/{id}` | TESORERO | Anular (no cuenta en saldos ni informes) |
| GET | `/api/movimientos/{id}/comprobante` | DIRECTIVA, TESORERO | Imagen de la boleta/comprobante |
| GET | `/api/pagos` | DIRECTIVA, TESORERO | Pagos con vecino, sede, fecha de reserva, medio y estado |
| POST | `/api/pagos` | TESORERO | Pago manual — multipart `datos` (JSON) + `archivo` **obligatorio** |
| POST | `/api/pagos/webpay/iniciar` | VECINO (dueño) | Inicia Webpay → `{url, token}` |
| GET / POST | `/api/pagos/webpay/retorno` | Público | Vuelta desde Transbank; responde una página HTML |
| GET | `/api/informes` | Autenticado | Informes (el VECINO solo ve los publicados) |
| GET | `/api/informes/{id}` | Autenticado | Informe + totales calculados |
| POST | `/api/informes` | TESORERO | Crear informe |

Los cuerpos y respuestas de ejemplo de cada endpoint están en [docs/API.md](docs/API.md) y en la colección de Postman.

**Formato de error** (400, 401, 403, 404, 409):
```json
{ "status": 409, "error": "Conflict", "mensaje": "La sede ya está reservada en ese horario", "timestamp": "2026-11-18T09:11:44.82" }
```

**Multipart (movimientos y pagos):** la parte `datos` debe enviarse con `Content-Type: application/json`.
En Postman: en *form-data*, columna *Content Type* de `datos` = `application/json`. En Retrofit:
`json.toRequestBody("application/json".toMediaType())`.

---

## 7. Probar Webpay Plus (ambiente de integración)

Webpay está conectado al **ambiente de integración de Transbank** (`webpay3gint.transbank.cl`): las tarjetas son de
prueba y **no hay cobros reales**.

### Flujo
1. El vecino dueño de una reserva **APROBADA y no pagada** llama a `POST /api/pagos/webpay/iniciar` con `{"reservaId": 2}`.
   El **monto lo pone el servidor** (precio de la sede); la app no lo envía.
2. La respuesta es `{"url": "https://webpay3gint.transbank.cl/webpayserver/initTransaction", "token": "01ab..."}`.
3. Abre en el navegador **`url + "?token_ws=" + token`** (en Android: Custom Tab o navegador).
4. Se paga en el formulario de Transbank con los datos de prueba (abajo).
5. Transbank redirige a `WEBPAY_RETURN_URL` (`/api/pagos/webpay/retorno`). El backend confirma la transacción y muestra una
   página "Pago aprobado/rechazado, vuelve a la app" que redirige a **`zaror://pago?estado=ok`** o **`zaror://pago?estado=error`**.
6. La app recarga `GET /api/reservas/mias` para ver `"pagada": true`.

### Datos de prueba de Transbank

| Dato | Valor | Resultado |
|------|-------|-----------|
| Tarjeta VISA | **4051 8856 0044 6623** | Aprobada |
| CVV | **123** | |
| Fecha de expiración | cualquier fecha futura | |
| RUT (autenticación del banco) | **11.111.111-1** | |
| Clave | **123** | |

Fuente: documentación de Transbank Developers → *Cómo empezar → Ambiente de integración → Tarjetas de prueba*.

### ¿Desde dónde lo pruebo?
- **Desde el emulador Android:** sirve el valor por defecto `WEBPAY_RETURN_URL=http://10.0.2.2:8080/api/pagos/webpay/retorno`
  (`10.0.2.2` es el PC visto desde el emulador).
- **Desde el navegador del PC:** cambia en `.env` `WEBPAY_RETURN_URL=http://localhost:8080/api/pagos/webpay/retorno`
  y reinicia ms-finanza (Docker: `docker compose up -d ms-finanza`).

### Qué esperar
**Pago aprobado (VISA):**
- Página **"Pago aprobado"** → `zaror://pago?estado=ok`.
- `GET /api/reservas/mias` → la reserva 2 con `"pagada": true`.
- `GET /api/pagos` → nuevo pago `"medio": "WEBPAY"`, `"estado": "PAGADO"`, `"monto": 35000`, `"referencia"` = código de autorización.
- `GET /api/movimientos?anio=2026&mes=11` → nuevo INGRESO "Arriendo Salón principal - Camila Rojas (2026-11-28)" por $35.000.

**Pago rechazado:** en el formulario de Transbank, presionar **"Anular compra"** → el pago queda **RECHAZADO**
(lo mismo ocurre si se agota el tiempo del formulario).
- Página **"Pago rechazado"** → `zaror://pago?estado=error`.
- El pago queda `RECHAZADO`, la reserva sigue `"pagada": false` y se puede intentar de nuevo.

Recargar la página de retorno no confirma el pago dos veces.

---

## 8. Pruebas

### Script automático (Git Bash)
```bash
bash scripts/prueba-completa.sh          # ~56 pruebas vía gateway; sirve con Docker o con el IDE
bash scripts/prueba-completa.sh --ide    # + pruebas llamando directo a los ms (solo con la Opción B)
```
Cubre todos los endpoints, roles (403), reglas de negocio (409/400) y seguridad JWT: sin token, token vencido,
firmado con otro secreto, rol alterado, `alg: none` y bloqueo de `/interno`. Termina con `RESULTADO: N OK, 0 FAIL`.
Deja algunos registros de prueba (reserva cancelada, movimiento anulado, un usuario y un informe).

### Postman
Importa [postman/Junta-Zaror.postman_collection.json](postman/Junta-Zaror.postman_collection.json).
Ejecuta primero la carpeta **"0. Login"**: guarda `tokenVecino`, `tokenVecino2`, `tokenDirectiva` y `tokenTesorero`.
En los multipart elige un JPG/PNG en el campo `archivo`.

### Swagger (solo Opción B, directo a cada ms)
`http://localhost:8081/swagger-ui.html` (y 8082, 8083, 8084).

---

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---------|------------------|
| `503` justo después de levantar | El gateway aún no ve los ms en Eureka. Espera 30–60 s. |
| `401 Token inválido` en un solo ms | Ese ms tiene otro `JWT_SECRET`. Todos deben leer el mismo `.env`. |
| `docker compose` dice `Falta JWT_SECRET en .env` | No existe el `.env` o le falta esa línea (sección 3). |
| `Access denied for user` en los logs | Cambiaste las contraseñas del `.env` después de crear MySQL → `docker compose down -v`. |
| `400 "datos debe enviarse como application/json"` | En el multipart, la parte `datos` necesita Content-Type `application/json`. |
| `400 "El cuerpo de la petición es inválido"` con curl en Windows | Tildes/ñ mal codificadas en la consola; usa Postman o `--data-binary @archivo.json`. |
| Las fotos de sedes dan 404 | El nombre no coincide o falta el JPG en `ms-reserva/src/main/resources/static/fotos/`; reconstruye ms-reserva (`docker compose up -d --build ms-reserva`). |

---

## 10. Estructura

```
appzaror/
├── docker-compose.yml          # 7 servicios (o solo mysql para el IDE)
├── .env.example                # plantilla de variables (el .env real no se sube)
├── docker/mysql-init/          # script que crea db_auth, db_usuario, db_reserva, db_finanza
├── docs/API.md                 # referencia de endpoints con cuerpos y respuestas
├── postman/                    # colección de Postman
├── scripts/prueba-completa.sh  # pruebas automáticas
├── .vscode/launch.json         # correr los ms desde VS Code con el .env
└── ms-eureka | ms-gateway | ms-auth | ms-usuario | ms-reserva | ms-finanza
    ├── Dockerfile              # multi-stage: maven:3.9-eclipse-temurin-21 -> eclipse-temurin:21-jre
    └── src/main/java/com/zaror/<ms>/
        model · repository · dto · service · controller · config · security · client
```
