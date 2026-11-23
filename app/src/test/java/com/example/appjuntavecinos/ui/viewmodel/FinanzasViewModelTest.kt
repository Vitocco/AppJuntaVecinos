package com.example.appjuntavecinos.ui.viewmodel

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.Datos.hoy
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.InformeRepository
import com.example.appjuntavecinos.data.session.SesionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Finanzas (Vecino y Directiva): resumen, movimientos (según rol) e informes. */
@OptIn(ExperimentalCoroutinesApi::class)
class FinanzasViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val finanzas = mockk<FinanzasRepository>()
    private val informes = mockk<InformeRepository>()

    @Before
    fun preparar() {
        mockkObject(SesionManager)
        coEvery { finanzas.resumen(any(), any()) } returns Resultado.Exito(Datos.resumen)
        coEvery { informes.informes() } returns Resultado.Exito(emptyList())
    }

    @After
    fun limpiar() = unmockkAll()

    private fun conRol(rol: String) {
        every { SesionManager.sesion } returns MutableStateFlow(Datos.sesion(rol))
    }

    @Test
    fun `la directiva tambien ve los movimientos del mas nuevo al mas viejo`() = runTest {
        // Preparar
        conRol("DIRECTIVA")
        val viejo = Datos.movimiento(1, hoy.minusDays(3))
        val nuevo = Datos.movimiento(2, hoy)
        coEvery { finanzas.movimientos(any(), any()) } returns Resultado.Exito(listOf(viejo, nuevo))
        val vm = FinanzasViewModel(finanzas, informes)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertTrue(vm.verMovimientos)
        assertEquals(EstadoUi.Listo(listOf(nuevo, viejo)), vm.movimientos.value)
    }

    @Test
    fun `error en informes muestra el error`() = runTest {
        // Preparar
        conRol("VECINO")
        coEvery { informes.informes() } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = FinanzasViewModel(finanzas, informes)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.informes.value)
    }
}
