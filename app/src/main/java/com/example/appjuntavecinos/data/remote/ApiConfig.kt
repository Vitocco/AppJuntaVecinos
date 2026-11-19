package com.example.appjuntavecinos.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrl

// Configuración de la conexión con el backend. ÚNICO lugar donde está la dirección del servidor.
object ApiConfig {
    // 10.0.2.2 es "localhost del PC" visto desde el emulador de Android.
    // Las rutas de ApiService van SIN "/" inicial (ej: "api/auth/login").
    const val BASE_URL = "http://10.0.2.2:8080/"

    // Host del backend: el interceptor solo manda el token a este servidor
    val HOST: String = BASE_URL.toHttpUrl().host

    // Las fotos llegan como ruta relativa ("/api/sedes/fotos/salon-1.jpg").
    // Se les antepone el servidor (sin la "/" final para no duplicarla).
    fun urlCompleta(ruta: String?): String? =
        ruta?.let { BASE_URL.trimEnd('/') + "/" + it.trimStart('/') }

    // Imagen del comprobante de un movimiento (requiere token: Coil usa nuestro OkHttp)
    fun urlComprobante(movimientoId: Long): String =
        BASE_URL + "api/movimientos/$movimientoId/comprobante"
}
