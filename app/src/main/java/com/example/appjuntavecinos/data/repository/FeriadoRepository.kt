package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.remote.FeriadoApi
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.Feriado
import com.example.appjuntavecinos.data.remote.llamarApi

class FeriadoRepository(private val api: FeriadoApi) {

    /** Feriados de Chile de un año (Nager.Date). Sin caché. */
    suspend fun obtenerFeriados(anio: Int): Resultado<List<Feriado>> =
        llamarApi { api.obtenerFeriados(anio) }
}
