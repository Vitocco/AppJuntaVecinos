package com.example.appjuntavecinos.data.model

data class ReservaForm(                 // = FormularioModel
    val fecha: Long? = null,
    val horaInicio: Int? = null,
    val horaTermino: Int? = null,
    val motivo: String = "",
    val asistentes: String = ""         // String porque viene de un TextField
)
