package com.example.appjuntavecinos.navigation

/**
 * Rutas de la app.
 * Nivel superior: login, registro y UN destino por rol. Cada rol tiene su propio
 * grafo (NavHost interno) con sus pestañas, así un rol no puede abrir pantallas de otro.
 */
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    const val VECINO = "vecino"
    const val DIRECTIVA = "directiva"
    const val TESORERO = "tesorero"

    // Rutas a las que se puede entrar SIN sesión
    val publicas = setOf(LOGIN, REGISTRO)

    /** Destino después del login, según el rol que devolvió el backend. */
    fun inicioDe(rol: String): String = when (rol) {
        "DIRECTIVA" -> DIRECTIVA
        "TESORERO" -> TESORERO
        else -> VECINO
    }
}

/** Grafo del VECINO:  Inicio | Reservas | Finanzas | Perfil */
object RutasVecino {
    const val INICIO = "inicio"
    const val RESERVAS = "reservas"
    const val FINANZAS = "finanzas"
    const val PERFIL = "perfil"
    const val SOLICITAR = "solicitar"
    const val INFORME = "informe/{id}"
    fun informe(id: Long) = "informe/$id"
    // Formulario de Webpay en un WebView (la URL va codificada; la reserva sirve para reintentar)
    const val WEBPAY = "webpay?url={url}&reserva={reserva}"
    fun webpay(p: com.example.appjuntavecinos.util.AbrirWebpay) =
        "webpay?url=${android.net.Uri.encode(p.url)}&reserva=${p.reservaId}"
}

/** Grafo de la DIRECTIVA:  Inicio | Solicitudes | Calendario | Finanzas | Perfil */
object RutasDirectiva {
    const val INICIO = "inicio"
    const val SOLICITUDES = "solicitudes"
    const val CALENDARIO = "calendario"
    const val FINANZAS = "finanzas"
    const val PERFIL = "perfil"
    const val SEDE = "sede/{sedeId}"
    fun sede(id: Long) = "sede/$id"
    const val INFORME = "informe/{id}"
    fun informe(id: Long) = "informe/$id"
}

/** Grafo del TESORERO:  Inicio | Movimientos | Pagos | Informes | Perfil */
object RutasTesorero {
    const val INICIO = "inicio"
    const val MOVIMIENTOS = "movimientos"
    const val PAGOS = "pagos"
    const val INFORMES = "informes"
    const val PERFIL = "perfil"

    // Registrar o editar movimiento. Parámetros opcionales:
    //  tipo = INGRESO|GASTO, id + fecha = editar ese movimiento, camara = abrir la cámara al entrar
    const val MOVIMIENTO = "movimiento?tipo={tipo}&id={id}&fecha={fecha}&camara={camara}"
    fun nuevoMovimiento(tipo: String, camara: Boolean = false) = "movimiento?tipo=$tipo&camara=$camara"
    fun editarMovimiento(id: Long, fecha: String) = "movimiento?id=$id&fecha=$fecha"

    const val PAGO = "pago/{reservaId}"
    fun pago(reservaId: Long) = "pago/$reservaId"

    const val INFORME = "informe/{id}"
    fun informe(id: Long) = "informe/$id"
    const val NUEVO_INFORME = "nuevo_informe"
}
