package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.data.local.ReservaCacheDao
import com.example.appjuntavecinos.data.local.aEntity
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.session.SesionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ReservaRepositoryTest {

    private val api = mockk<ApiService>()
    private val dao = mockk<ReservaCacheDao>()
    private val repositorio = ReservaRepository(api, dao)

    private val r1 = Datos.reserva(id = 1)
    private val r2 = Datos.reserva(id = 2, estado = "PENDIENTE")

    @Before
    fun preparar() {
        // usuario logueado con id 7 (la caché se guarda por usuario)
        mockkObject(SesionManager)
        every { SesionManager.sesion } returns MutableStateFlow(Datos.sesion())
    }

    @After
    fun limpiar() = unmockkAll()

    @Test
    fun `con la API OK actualiza la cache de Room del usuario`() = runTest {
        // Preparar
        coEvery { api.misReservas() } returns listOf(r1, r2)
        coEvery { dao.reemplazarMias(any(), any()) } just runs

        // Ejecutar
        val r = repositorio.misReservas()

        // Verificar
        assertEquals(Resultado.Exito(listOf(r1, r2)), r)
        coVerify(exactly = 1) { dao.reemplazarMias(7, listOf(r1.aEntity(), r2.aEntity())) }
    }

    @Test
    fun `con la API caida devuelve la cache marcada como desdeCache`() = runTest {
        // Preparar
        coEvery { api.misReservas() } throws IOException("sin red")
        coEvery { dao.misReservas(7) } returns listOf(r1.aEntity())

        // Ejecutar
        val r = repositorio.misReservas()

        // Verificar
        assertEquals(Resultado.Exito(listOf(r1), desdeCache = true), r)
        coVerify(exactly = 0) { dao.reemplazarMias(any(), any()) }
    }
}
