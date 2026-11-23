package com.example.appjuntavecinos.data.remote.dto

// --------------------------- FERIADOS (Nager.Date) ---------------------------

// GET api/v3/publicholidays/{anio}/CL   (API pública externa, no es nuestro backend)
data class Feriado(
    val date: String,       // "2026-12-25"
    val localName: String   // "Navidad"
)
