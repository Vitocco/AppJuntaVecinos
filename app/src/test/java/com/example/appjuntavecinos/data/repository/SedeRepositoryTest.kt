package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.data.local.SedeDao
import com.example.appjuntavecinos.data.local.aEntity
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class SedeRepositoryTest {

    private val api = mockk<ApiService>()
    private val dao = mockk<SedeDao>()
    private val repositorio = SedeRepository(api, dao)

    @Test
    fun `con la API OK guarda las sedes en Room`() = runTest {
        // Preparar
        val sedes = listOf(Datos.sede(1), Datos.sede(2))
        coEvery { api.sedes() } returns sedes
        coEvery { dao.reemplazar(any()) } just runs

        // Ejecutar
        val r = repositorio.obtenerSedes()

        // Verificar
        assertEquals(Resultado.Exito(sedes), r)
        coVerify(exactly = 1) { dao.reemplazar(sedes.map { it.aEntity() }) }
    }

    @Test
    fun `con la API caida devuelve las sedes guardadas`() = runTest {
        // Preparar
        coEvery { api.sedes() } throws IOException("sin red")
        coEvery { dao.obtenerTodas() } returns listOf(Datos.sede(1).aEntity())

        // Ejecutar
        val r = repositorio.obtenerSedes()

        // Verificar
        assertEquals(Resultado.Exito(listOf(Datos.sede(1)), desdeCache = true), r)
    }
}
