package com.example.appjuntavecinos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,
    val tipo: String,
    val categoria: String,
    val monto: Int
)