package com.example.appjuntavecinos.data.remote.dto

// ---------------------------- SEDES ----------------------------

// GET api/sedes   (fotos = rutas relativas: "/api/sedes/fotos/salon-1.jpg")
data class SedeDto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val ubicacion: String?,
    val capacidad: Int,
    val precio: Long,
    val activa: Boolean,
    val fotos: List<String>?
)

// GET api/sedes/{id}/disponibilidad?anio=&mes=  (horarios ocupados, sin datos personales)
data class OcupacionDto(
    val fecha: String,       // "2026-11-28"
    val horaInicio: String,  // "15:00"
    val horaFin: String,
    val estado: String       // PENDIENTE | APROBADA
)

// --------------------------- RESERVAS ---------------------------

// Objeto reserva: lo devuelven todos los endpoints de reservas
data class ReservaDto(
    val id: Long,
    val sedeId: Long,
    val sedeNombre: String?,
    val sedeFoto: String?,
    val usuarioId: Long,
    val usuarioNombre: String?,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val motivo: String?,
    val asistentes: Int,
    val estado: String,      // PENDIENTE | APROBADA | RECHAZADA | CANCELADA
    val pagada: Boolean,
    val precio: Long,
    val observacion: String?
)

// POST api/reservas
data class CrearReservaRequest(
    val sedeId: Long,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val motivo: String,
    val asistentes: Int
)

// PUT api/reservas/{id}/estado   (estado: solo APROBADA o RECHAZADA)
data class CambiarEstadoRequest(
    val estado: String,
    val observacion: String?
)
