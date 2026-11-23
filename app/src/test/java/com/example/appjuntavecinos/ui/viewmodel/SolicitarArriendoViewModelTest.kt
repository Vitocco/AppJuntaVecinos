package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.CrearReservaRequest
import com.example.appjuntavecinos.data.remote.dto.OcupacionDto
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.repository.SedeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitarArriendoViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val sedes = mockk<SedeRepository>()
    private val reservas = mockk<ReservaRepository>()

    /** Crea el ViewModel (su init carga las sedes) y deja la sede 1 (capacidad 50) elegida. */
    private fun TestScope.crear(ocupaciones: List<OcupacionDto> = emptyList()): SolicitarArriendoViewModel {
        coEvery { sedes.obtenerSedes() } returns Resultado.Exito(listOf(Datos.sede(1, capacidad = 50)))
        coEvery { sedes.disponibilidad(any(), any(), any()) } returns Resultado.Exito(ocupaciones)
        val vm = SolicitarArriendoViewModel(mockk<Application>(relaxed = true), sedes, reservas)
        advanceUntilIdle()
        return vm
    }

    private fun SolicitarArriendoViewModel.llenarValido() {
        elegirDia(dias[2])
        elegirDesde("18:00")
        elegirHasta("22:00")
        actualizarMotivo("Cumpleaños de Camila")
        actualizarPersonas("20")
    }

    private val mesesOfrecidos get() = (1..21).map { YearMonth.from(Datos.hoy.plusDays(it.toLong())) }.distinct().size

    @Test
    fun `reserva valida se envia al backend`() = runTest {
        // Preparar
        val vm = crear()
        val enviada = io.mockk.slot<CrearReservaRequest>()
        coEvery { reservas.crear(capture(enviada)) } returns Resultado.Exito(Datos.reserva(estado = "PENDIENTE"))
        vm.llenarValido()

        // Ejecutar
        vm.enviar()
        advanceUntilIdle()

        // Verificar
        assertTrue(vm.enviada.value)
        assertFalse(vm.enviando.value)
        assertEquals(
            CrearReservaRequest(1, vm.dias[2].toString(), "18:00", "22:00", "Cumpleaños de Camila", 20),
            enviada.captured
        )
    }

    @Test
    fun `choque de horario 409 muestra el error y recarga la disponibilidad`() = runTest {
        // Preparar
        val vm = crear()
        coEvery { reservas.crear(any()) } returns Resultado.Error("La sede ya está reservada en ese horario", 409)
        vm.llenarValido()

        // Ejecutar
        vm.enviar()
        advanceUntilIdle()

        // Verificar
        assertEquals("La sede ya está reservada en ese horario", vm.mensajeError.value)
        assertFalse(vm.enviada.value)
        // una vez al abrir + otra después del 409
        coVerify(exactly = mesesOfrecidos * 2) { sedes.disponibilidad(1, any(), any()) }
    }

    @Test
    fun `sin dia ni horario elegido muestra los errores`() = runTest {
        // Preparar
        val vm = crear()

        // Ejecutar
        vm.enviar()
        advanceUntilIdle()

        // Verificar
        val e = vm.errores.value
        assertEquals("Elige un día", e.fecha)
        assertEquals("Elige la hora de inicio", e.desde)
        assertEquals("Elige la hora de término", e.hasta)
        assertEquals("El motivo es obligatorio", e.motivo)
        assertEquals("Ingresa un número", e.personas)
        coVerify(exactly = 0) { reservas.crear(any()) }
    }

    @Test
    fun `hora de termino antes del inicio muestra error`() = runTest {
        // Preparar
        val vm = crear()
        vm.llenarValido()
        vm.elegirDesde("20:00")
        vm.elegirHasta("18:00")

        // Ejecutar
        vm.enviar()
        advanceUntilIdle()

        // Verificar
        assertEquals("Debe ser después del inicio", vm.errores.value.hasta)
        coVerify(exactly = 0) { reservas.crear(any()) }
    }
}
