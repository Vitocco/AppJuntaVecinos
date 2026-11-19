package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.local.SedeDao
import com.example.appjuntavecinos.data.local.aDto
import com.example.appjuntavecinos.data.local.aEntity
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.OcupacionDto
import com.example.appjuntavecinos.data.remote.dto.SedeDto
import com.example.appjuntavecinos.data.remote.llamarApi

class SedeRepository(
    private val api: ApiService,
    private val dao: SedeDao
) {
    /**
     * Sedes: primero el backend (y se guarda en Room). Si NO hay conexión y hay
     * algo guardado, se devuelve la caché marcada como desdeCache = true.
     */
    suspend fun obtenerSedes(): Resultado<List<SedeDto>> {
        val resultado = llamarApi { api.sedes() }
        return when {
            resultado is Resultado.Exito -> {
                dao.reemplazar(resultado.datos.map { it.aEntity() })
                resultado
            }
            resultado is Resultado.Error && resultado.sinConexion -> {
                val guardadas = dao.obtenerTodas()
                if (guardadas.isNotEmpty()) Resultado.Exito(guardadas.map { it.aDto() }, desdeCache = true)
                else resultado
            }
            else -> resultado
        }
    }

    /** Horarios ocupados de una sede en un mes (PENDIENTE y APROBADA). Sin caché: debe ser al día. */
    suspend fun disponibilidad(sedeId: Long, anio: Int, mes: Int): Resultado<List<OcupacionDto>> =
        llamarApi { api.disponibilidad(sedeId, anio, mes) }
}
