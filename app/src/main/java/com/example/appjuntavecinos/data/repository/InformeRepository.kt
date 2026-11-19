package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.CrearInformeRequest
import com.example.appjuntavecinos.data.remote.dto.InformeDto
import com.example.appjuntavecinos.data.remote.llamarApi

// Informes / rendiciones. El vecino solo recibe los publicados (lo filtra el backend).
class InformeRepository(private val api: ApiService) {

    suspend fun informes(): Resultado<List<InformeDto>> = llamarApi { api.informes() }

    suspend fun detalle(id: Long): Resultado<InformeDto> = llamarApi { api.informe(id) }

    suspend fun crear(datos: CrearInformeRequest): Resultado<InformeDto> = llamarApi { api.crearInforme(datos) }
}
