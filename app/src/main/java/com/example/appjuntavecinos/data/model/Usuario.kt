package com.example.appjuntavecinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// La tabla "usuarios" (igual que Note.kt del profe)
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val telefono: String,
    val numeroCasa: Int,
    val fechaNacimiento: Long,
    val passwordHash: String,     // SHA-256, nunca la contraseña real
    val rol: String,              // "VECINO" o "SOCIO"
    val recibirAvisos: Boolean
)
