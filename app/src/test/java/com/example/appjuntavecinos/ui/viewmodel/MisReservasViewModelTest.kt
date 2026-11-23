package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.Datos.hoy
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarResponse
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.util.AbrirWebpay
import com.example.appjuntavecinos.util.Webpay
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MisReservasViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val reservas = mockk<ReservaRepository>()
    private val pagos = mockk<PagoRepository>()

    private fun crear() = MisReservasViewModel(mockk<Application>(relaxed = true), reservas, pagos)

    @After
    fun limpiar() = unmockkAll()

    @Test
    fun `cargar lista las reservas con las proximas primero y luego las pasadas`() = runTest {
        // Preparar
        val pasadaVieja = Datos.reserva(id = 1, fecha = hoy.minusDays(30))
        val pasadaReciente = Datos.reserva(id = 2, fecha = hoy.minusDays(2))
        val lejana = Datos.reserva(id = 3, fecha = hoy.plusDays(20))
        val cercana = Datos.reserva(id = 4, fecha = hoy.plusDays(1))
        coEvery { reservas.misReservas() } returns Resultado.Exito(listOf(pasadaVieja, lejana, pasadaReciente, cercana))
        val vm = crear()

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(listOf(cercana, lejana, pasadaReciente, pasadaVieja)), vm.estado.value)
    }

    @Test
    fun `error al cargar muestra el error`() = runTest {
        // Preparar
        coEvery { reservas.misReservas() } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = crear()

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.estado.value)
    }

    @Test
    fun `cancelar una reserva avisa y recarga la lista`() = runTest {
        // Preparar
        val r = Datos.reserva(id = 5, estado = "PENDIENTE")
        coEvery { reservas.cancelar(5) } returns Resultado.Exito(r.copy(estado = "CANCELADA"))
        coEvery { reservas.misReservas() } returns Resultado.Exito(listOf(r.copy(estado = "CANCELADA")))
        val vm = crear()
        vm.pedirCancelar(r)

        // Ejecutar
        vm.confirmarCancelar()
        advanceUntilIdle()

        // Verificar
        assertEquals("Reserva cancelada", vm.mensaje.value)
        assertNull(vm.porCancelar.value)
        assertFalse(vm.cancelando.value)
        coVerify(exactly = 1) { reservas.misReservas() }
        assertEquals("CANCELADA", (vm.estado.value as EstadoUi.Listo).datos.single().estado)
    }

    @Test
    fun `pagar con Webpay entrega la URL del formulario`() = runTest {
        // Preparar
        mockkObject(Webpay)
        every { Webpay.urlFormulario(any()) } returns "https://webpay.cl/form?token_ws=tok"
        val r = Datos.reserva(id = 8)
        coEvery { pagos.iniciarWebpay(8) } returns Resultado.Exito(WebpayIniciarResponse("https://webpay.cl/form", "tok"))
        val vm = crear()
        vm.abrirHojaPago(r)

        // Ejecutar
        vm.pagarConWebpay()
        advanceUntilIdle()

        // Verificar
        assertEquals(AbrirWebpay(8, "https://webpay.cl/form?token_ws=tok"), vm.abrirUrl.value)
        assertNull(vm.hojaPago.value)
        assertNull(vm.pagando.value)
        vm.urlAbierta()
        assertNull(vm.abrirUrl.value)
    }
}
