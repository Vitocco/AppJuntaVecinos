package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.data.remote.dto.Feriado
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API pública Nager.Date (feriados). Las rutas van SIN "/" inicial porque
 * FeriadosClient.BASE_URL ya termina en "/". No lleva token.
 */
interface FeriadoApi {

    @GET("api/v3/publicholidays/{anio}/CL")
    suspend fun obtenerFeriados(@Path("anio") anio: Int): List<Feriado>
}
