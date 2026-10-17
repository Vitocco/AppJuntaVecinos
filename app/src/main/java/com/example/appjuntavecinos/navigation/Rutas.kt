package com.example.appjuntavecinos.navigation

// Los nombres de todas las pantallas en UN solo lugar.
// ¿Por qué no escribir "inicio" directo? Si te equivocas escribiendo "incio" en algún lado,
// la app se cae y cuesta encontrar el error. Con constantes, Android Studio te avisa al tiro.
object Rutas {
    const val LOGIN = "login"
    const val INICIO = "inicio"
    // aquí irás agregando: RESERVA, MIS_PAGOS, HISTORIAL, GESTION, MOVIMIENTO...

    const val RESERVA = "reserva" //pantalla 3

    const val HISTORIAL = "historial" //pantalla 5
}