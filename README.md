# Junta Zaror

Aplicación móvil Android para la **Junta de Vecinos Población Zaror**. Permite que los vecinos se registren, soliciten la reserva de la sede y consulten la información financiera, y que la directiva (socios) registre los movimientos de dinero con su comprobante.

Proyecto semestral del ramo **DSY1105 – Desarrollo de Aplicaciones Móviles**, sección **010D** (DUOC UC). Profesor: Carlos Abarzúa.

**Integrantes:** Victor Cortez · Catalina Correa

---

## 1. Tecnologías

| Tecnología | Para qué se usa |
|---|---|
| Kotlin | Lenguaje de toda la app |
| Jetpack Compose + Material 3 | Interfaz de usuario (pantallas como funciones `@Composable`) |
| MVVM | Separación entre pantalla, lógica y datos |
| StateFlow | Estado de cada pantalla, que se redibuja al cambiar |
| Room (KSP) | Base de datos local SQLite |
| Navigation Compose | Navegación entre pantallas |
| WorkManager | Notificación local con la app cerrada |
| Cámara (`TakePicture`) + FileProvider | Foto del comprobante |

Configuración: `minSdk 24`, Android Studio con Gradle Kotlin DSL.

---

## 2. Funcionalidades

### Vecino
- **Registro** con validación de todos los campos y correo único.
- **Login** con contraseña protegida (hash SHA-256).
- **Inicio** con resumen y acceso a las demás secciones.
- **Solicitar reserva** de la sede: fecha, hora de inicio y término, asistentes. Valida que no choque con otra reserva.
- **Notificación**: al enviar la solicitud, 10 segundos después llega un aviso aunque la app esté cerrada.
- **Mis pagos** (en desarrollo).

### Socio (directiva)
- Login con rol `SOCIO`, que lleva a su propio panel.
- **Registrar movimiento** (ingreso o gasto) con categoría, monto y **foto del comprobante** tomada con la cámara.
- **Historial financiero**: totales del mes, gastos por categoría, informes y rendiciones.

---

## 3. Arquitectura

Se usa **MVVM por capas**. Cada capa tiene una sola responsabilidad:

```
Screen  →  ViewModel  →  Repository  →  DAO  →  Room (base de datos)
(muestra)  (estado y      (intermediario)  (operaciones     (donde viven
           lógica)                          sobre la base)   los datos)
```

| Capa | Qué hace | Ejemplo |
|---|---|---|
| **Screen** | Dibuja la interfaz y avisa los clics. No tiene lógica. | `RegistroScreen` |
| **ViewModel** | Guarda el estado, valida y decide. Sobrevive a la rotación. | `RegistroViewModel` |
| **Repository** | Intermediario entre el ViewModel y la base. | `UsuarioRepository` |
| **DAO** | Interface con las operaciones (insertar, consultar). Room genera la implementación. | `UsuarioDao` |
| **Model** | Define los datos: tablas (`@Entity`) y formularios. | `Usuario`, `RegistroModel` |

### Estado con StateFlow

Cada ViewModel tiene "una caja con dos accesos":

```kotlin
private val _formulario = MutableStateFlow(RegistroModel())  // el ViewModel escribe
val formulario = _formulario.asStateFlow()                   // la Screen solo lee
```

La Screen observa con `collectAsState()` y se redibuja cada vez que el valor cambia. Como la caja vive en el ViewModel, **lo escrito no se pierde al girar el celular**.

Cada formulario usa dos cajas: `formulario` (lo que el usuario escribe) y `errores` (mensaje por campo; `null` significa que no hay error).

---

## 4. Estructura del proyecto

```
com.example.appjuntavecinos
├── MainActivity.kt            Única Activity: aplica el tema y muestra AppNavigation
├── data
│   ├── local                  Persistencia en el dispositivo
│   │   ├── AppDatabase.kt     Crea la base de datos Room (junta_database)
│   │   ├── UsuarioDao.kt
│   │   ├── ReservaDao.kt
│   │   └── FinanzasDao.kt     Movimientos e informes
│   ├── model
│   │   ├── Usuario, Reserva, Movimiento, Informe        → tablas (@Entity)
│   │   ├── LoginModel, RegistroModel, ReservaForm,
│   │   │   MovimientoForm                                → formularios (en memoria)
│   │   └── GastoCategoria, ResumenMes                    → resultados para mostrar
│   └── repository             UsuarioRepository, ReservaRepository, FinanzasRepository
├── navigation
│   ├── AppNavigation.kt       NavHost: qué pantalla se muestra en cada ruta
│   └── Rutas.kt               Nombres de las rutas en un solo lugar
├── ui
│   ├── components             BottomBar, BottomBarSocio, CampoHora, Orientation
│   ├── screens                Login, Registro, Inicio, SolicitarReserva, MisPagos,
│   │                          SocioHome, RegistrarMovimiento, HistorialFinanciero
│   ├── theme                  Color, Type, Theme
│   └── viewmodel              Login, Registro, Reserva, Movimiento, Historial
└── util
    ├── Seguridad.kt           sha256()
    ├── AvisoWorker.kt         Notificación con WorkManager
    ├── CamaraUriFactory.kt    Crea el archivo y el Uri para la foto
    └── Fechas.kt              Formato de fechas, horas y montos
```

---

## 5. Base de datos (Room)

Archivo `junta_database`, versión 5. Se crea en `AppDatabase.kt`.

| Tabla | Entidad | Contenido |
|---|---|---|
| `usuarios` | `Usuario` | Datos del vecino o socio, `passwordHash` y `rol` |
| `reservas` | `Reserva` | Solicitudes de la sede |
| `movimientos` | `Movimiento` | Ingresos y gastos, con la ruta del comprobante |
| `informes` | `Informe` | Informes y rendiciones publicadas |

Consultas principales:

- `UsuarioDao.buscarPorEmail(email)`: login y validación de correo único.
- `ReservaDao.buscarChoques(fecha, inicio, termino)`: evita reservas que se superponen.
- `FinanzasDao.totalDelMes`, `gastosPorCategoria`, `netoHasta`, `informes`: datos del historial.

Todas las operaciones son `suspend`: acceden a disco y no deben bloquear la pantalla.

> Si se cambia una `@Entity`, hay que subir `version` en `@Database`. Con `fallbackToDestructiveMigration` la base se recrea.

Para ver los datos: **Android Studio → App Inspection → Database Inspector**.

---

## 6. Seguridad y validaciones

- **Contraseñas:** nunca se guardan en texto. `Seguridad.sha256()` genera el hash y es lo único que queda en la base. En el login se calcula el hash de lo escrito y se compara.
- **Validaciones en el ViewModel** (no en la Screen): campos obligatorios, formato de correo, contraseña, montos y horas. El resultado se publica en el StateFlow `errores` y la Screen muestra el mensaje bajo cada campo.
- **Correo único:** `RegistroViewModel.guardar()` consulta `buscarPorEmail` y, si ya existe, muestra "Este correo ya está registrado".

---

## 7. Recursos nativos

### Cámara (comprobante del movimiento)
1. Se pide el permiso `CAMERA`.
2. `CamaraUriFactory` crea un archivo vacío en `filesDir/comprobantes/comprobante_xxx.jpg` y obtiene su `Uri` con **FileProvider** (`file_paths.xml` y `<provider>` en el Manifest).
3. El contrato `TakePicture` abre la cámara y ella escribe la foto en ese `Uri`.
4. El `Uri` se guarda en `MovimientoForm.comprobante`. Al guardar, Room almacena **solo la ruta como texto**; la imagen queda como archivo.

### Notificación local (con la app cerrada)
- **WorkManager** con `OneTimeWorkRequest` y `setInitialDelay(10 s)`. El trabajo lo ejecuta `AvisoWorker` (un `CoroutineWorker`).
- El trabajador crea el canal de notificación y muestra el aviso solo si el usuario dio el permiso `POST_NOTIFICATIONS` (Android 13+), que se solicita en `InicioScreen`.
- Se dispara al enviar una solicitud de reserva: `AppNavigation` → `programarAviso(...)`.
- Es una notificación **local**: no usa servidor ni internet.

---

## 8. Rotación de pantalla

Dos partes complementarias:

1. **Se conservan los datos:** el estado vive en el ViewModel, que sobrevive al giro.
2. **Se adapta el diseño:** `ui/components/Orientation.kt` lee `LocalConfiguration` y, en horizontal, usa dos columnas (`DosColumnas`).

---

## 9. Navegación

`AppNavigation.kt` define un `NavHost` con `startDestination = Rutas.LOGIN`.

```
Login ──(rol VECINO)──► Inicio ──► Solicitar reserva ──► (notificación)
  │                        └─────► Mis pagos / Historial
  ├──(rol SOCIO)───────► Panel socio ──► Registrar movimiento (cámara)
  └──► Registro
```

La barra inferior (`BottomBar`) está definida en `ui/components` y cada Screen la coloca en su propio `Scaffold`. `AppNavigation` solo recibe `onSeleccionar` y navega.

---

## 10. ¿Dónde está cada cosa?

| Pregunta | Archivo |
|---|---|
| ¿Dónde se crea la base de datos? | `data/local/AppDatabase.kt` |
| ¿Dónde se calcula el SHA-256? | `util/Seguridad.kt` |
| ¿Dónde se valida el correo único? | `ui/viewmodel/RegistroViewModel.kt` → `guardar()` |
| ¿Dónde se crea el socio de prueba? | `data/repository/UsuarioRepository.kt` → `crearSocioDePrueba()` |
| ¿Dónde se decide a qué pantalla va el login? | `navigation/AppNavigation.kt` → `onLoginClick` |
| ¿Dónde se programa la notificación? | `util/AvisoWorker.kt` → `programarAviso()` |
| ¿Dónde se pide el permiso de notificaciones? | `ui/screens/InicioScreen.kt` |
| ¿Dónde se toma y guarda la foto? | `ui/screens/RegistrarMovimientoScreen.kt` y `util/CamaraUriFactory.kt` |
| ¿Dónde se adapta el diseño al girar? | `ui/components/Orientation.kt` |
| ¿Dónde está la barra inferior? | `ui/components/BottomBar.kt` y `BottomBarSocio.kt` |
| ¿Dónde se aplica el tema? | `MainActivity.kt` y `ui/theme/` |

---

## 11. Cómo ejecutar

1. Abrir la carpeta del proyecto en **Android Studio**.
2. Esperar a que Gradle sincronice.
3. Ejecutar en un emulador o en un celular (Android 7.0 o superior).
4. Aceptar los permisos de cámara y notificaciones cuando la app los pida.

### Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Socio | `socio@zaror.cl` | `1234` |
| Vecino | Se crea desde la pantalla **Registro** | La que se elija |

El socio se crea solo la primera vez que se abre el Login.

---

## 12. Limitaciones conocidas

- **Mis pagos** es una pantalla en desarrollo (muestra un texto provisorio).
- En el panel del socio, las pestañas **Gestión** y **Reportes** aún no tienen pantalla asociada.
- Los datos de informes del historial y los límites de la reserva (por ejemplo, 60 asistentes) son de ejemplo.
