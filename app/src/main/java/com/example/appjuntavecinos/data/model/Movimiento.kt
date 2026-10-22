package com.example.appjuntavecinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,        // inicio del día en milisegundos, igual que en Reserva
    val tipo: String,       // "INGRESO" o "GASTO"
    val categoria: String,  // "Arriendo", "Luz", "Gas", "Agua"...
    val monto: Int ,         // en pesos
    val detalle: String = "",
    val comprobante: String? = null //foto del comprobante opcional
)