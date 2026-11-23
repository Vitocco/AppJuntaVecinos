package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.errorHttp
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.CrearInformeRequest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InformeRepositoryTest {

    private val api = mockk<ApiService>()
    private val repositorio = InformeRepository(api)

    @Test
    fun `informes y detalle devuelven lo de la API`() = runTest {
        // Preparar
        val informe = Datos.informe(1, "2026-10-01")
        coEvery { api.informes() } returns listOf(informe)
        coEvery { api.informe(1) } returns informe

        // Ejecutar
        val lista = repositorio.informes()
        val detalle = repositorio.detalle(1)

        // Verificar
        assertEquals(Resultado.Exito(listOf(informe)), lista)
        assertEquals(Resultado.Exito(informe), detalle)
    }

    @Test
    fun `crear informe repetido devuelve el error del backend`() = runTest {
        // Preparar
        val datos = CrearInformeRequest("Rendición octubre", "2026-10-01", "2026-10-31", "", true)
        coEvery { api.crearInforme(datos) } throws errorHttp(409, "Ya existe un informe para ese período")

        // Ejecutar
        val r = repositorio.crear(datos)

        // Verificar
        assertEquals(Resultado.Error("Ya existe un informe para ese período", 409), r)
    }
}
