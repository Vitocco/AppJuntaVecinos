package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.data.remote.FeriadoApi
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class FeriadoRepositoryTest {

    private val api = mockk<FeriadoApi>()
    private val repositorio = FeriadoRepository(api)

    @Test
    fun `devuelve la lista de feriados de la API para el anio pedido`() = runTest {
        // Preparar
        val navidad = Datos.feriado(LocalDate.of(2026, 12, 25), "Navidad")
        coEvery { api.obtenerFeriados(2026) } returns listOf(navidad)

        // Ejecutar
        val r = repositorio.obtenerFeriados(2026)

        // Verificar
        assertEquals(Resultado.Exito(listOf(navidad)), r)
        coVerify(exactly = 1) { api.obtenerFeriados(2026) }
    }

    @Test
    fun `sin internet el error se propaga como sin conexion`() = runTest {
        // Preparar
        coEvery { api.obtenerFeriados(any()) } throws IOException("sin red")

        // Ejecutar
        val r = repositorio.obtenerFeriados(2026)

        // Verificar
        assertEquals(Resultado.Error(MENSAJE_SIN_CONEXION, null), r)
    }
}
