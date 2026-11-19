package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.local.ReservaCacheDao
import com.example.appjuntavecinos.data.local.aDto
import com.example.appjuntavecinos.data.local.aEntity
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.CambiarEstadoRequest
import com.example.appjuntavecinos.data.remote.dto.CrearReservaRequest
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.llamarApi
import com.example.appjuntavecinos.data.session.SesionManager

class ReservaRepository(
    private val api: ApiService,
    private val dao: ReservaCacheDao
) {
    /** Reservas del usuario logueado, con caché en Room si no hay conexión. */
    suspend fun misReservas(): Resultado<List<ReservaDto>> {
        val usuarioId = SesionManager.sesion.value?.id ?: 0L
        val resultado = llamarApi { api.misReservas() }
        return when {
            resultado is Resultado.Exito -> {
                dao.reemplazarMias(usuarioId, resultado.datos.map { it.aEntity() })
                resultado
            }
            resultado is Resultado.Error && resultado.sinConexion -> {
                val guardadas = dao.misReservas(usuarioId)
                if (guardadas.isNotEmpty()) Resultado.Exito(guardadas.map { it.aDto() }, desdeCache = true)
                else resultado
            }
            else -> resultado
        }
    }

    /** Nueva solicitud. 409 = "La sede ya está reservada en ese horario". */
    suspend fun crear(datos: CrearReservaRequest): Resultado<ReservaDto> =
        llamarApi { api.crearReserva(datos) }

    /** El dueño cancela su reserva (queda CANCELADA). */
    suspend fun cancelar(id: Long): Resultado<ReservaDto> =
        llamarApi { api.cancelarReserva(id) }

    /** Directiva / Tesorero: todas las reservas, o solo las de un estado. */
    suspend fun todas(estado: String? = null): Resultado<List<ReservaDto>> =
        llamarApi { api.reservas(estado) }

    /** Directiva: aprobar o rechazar (observación opcional). */
    suspend fun cambiarEstado(id: Long, estado: String, observacion: String?): Resultado<ReservaDto> =
        llamarApi { api.cambiarEstadoReserva(id, CambiarEstadoRequest(estado, observacion?.ifBlank { null })) }
}
