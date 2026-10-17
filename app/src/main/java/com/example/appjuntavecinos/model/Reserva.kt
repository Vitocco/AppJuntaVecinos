package com.example.appjuntavecinos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservas")
data class Reserva(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,        // inicio del día en milisegundos
    val horaInicio: Int,    // hora en punto: 8..23
    val horaTermino: Int,
    val motivo: String,
    val asistentes: Int,
    val estado: String = "PENDIENTE"
)