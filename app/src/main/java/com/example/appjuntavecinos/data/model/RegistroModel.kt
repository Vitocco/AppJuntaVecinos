package com.example.appjuntavecinos.data.model

data class RegistroModel(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val numeroCasa: String = "",
    val fechaNacimiento: Long? = null,
    val password: String = "",
    val confirmarPassword: String = "",
    val aceptaReglamento: Boolean = false,
    val recibirAvisos: Boolean = true
)