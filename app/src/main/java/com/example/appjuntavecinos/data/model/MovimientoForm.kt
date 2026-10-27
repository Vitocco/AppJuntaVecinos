package com.example.appjuntavecinos.data.model

import android.net.Uri
import com.example.appjuntavecinos.util.Fechas

data class MovimientoForm(
    val tipo: String = "GASTO",
    val categoria: String? = null,
    val monto: String = "",             // String porque viene de un TextField
    val fecha: Long = Fechas.hoy(),
    val detalle: String = "",
    val comprobante: Uri? = null        // foto opcional
)
