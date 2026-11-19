package com.example.appjuntavecinos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.dto.SedeDto

// =====================================================================
// Tablas de CACHÉ. La fuente oficial es siempre el backend; Room solo guarda
// la última respuesta para mostrarla si no hay conexión (con aviso "Sin conexión").
// =====================================================================

@Entity(tableName = "cache_sedes")
data class SedeEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val descripcion: String?,
    val ubicacion: String?,
    val capacidad: Int,
    val precio: Long,
    val activa: Boolean,
    val fotos: String          // Room no guarda listas: las rutas se unen con "|"
)

// "Mis reservas" del usuario. Se guarda el usuarioId para no mostrarle a un
// vecino las reservas de otro que haya usado el mismo celular.
@Entity(tableName = "cache_reservas")
data class ReservaEntity(
    @PrimaryKey val id: Long,
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
    val estado: String,
    val pagada: Boolean,
    val precio: Long,
    val observacion: String?
)

// ---------------- Conversiones DTO <-> Entity ----------------

fun SedeDto.aEntity() = SedeEntity(
    id, nombre, descripcion, ubicacion, capacidad, precio, activa,
    fotos = fotos.orEmpty().joinToString("|")
)

fun SedeEntity.aDto() = SedeDto(
    id, nombre, descripcion, ubicacion, capacidad, precio, activa,
    fotos = if (fotos.isEmpty()) emptyList() else fotos.split("|")
)

fun ReservaDto.aEntity() = ReservaEntity(
    id, sedeId, sedeNombre, sedeFoto, usuarioId, usuarioNombre, fecha, horaInicio,
    horaFin, motivo, asistentes, estado, pagada, precio, observacion
)

fun ReservaEntity.aDto() = ReservaDto(
    id, sedeId, sedeNombre, sedeFoto, usuarioId, usuarioNombre, fecha, horaInicio,
    horaFin, motivo, asistentes, estado, pagada, precio, observacion
)
