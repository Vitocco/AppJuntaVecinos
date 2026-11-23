package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.Datos.hoy
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.repository.ReservaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Directiva: Inicio (DirectivaInicioViewModel) y aprobar/rechazar (GestionReservasViewModel). */
@OptIn(ExperimentalCoroutinesApi::class)
class DirectivaInicioViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val reservas = mockk<ReservaRepository>()

    private fun crear() = DirectivaInicioViewModel(mockk<Application>(relaxed = true), reservas)

    @Test
    fun `inicio lista las pendientes ordenadas, cuenta las del mes y muestra 3 proximas aprobadas`() = runTest {
        // Preparar
        val pendienteLejana = Datos.reserva(id = 1, fecha = hoy.plusMonths(2), estado = "PENDIENTE")
        val pendienteHoy = Datos.reserva(id = 2, fecha = hoy, estado = "PENDIENTE")
        val aprobadaHoy = Datos.reserva(id = 3, fecha = hoy)
        val aprobada1 = Datos.reserva(id = 4, fecha = hoy.plusMonths(1))
        val aprobada3 = Datos.reserva(id = 5, fecha = hoy.plusMonths(3))
        val aprobada4 = Datos.reserva(id = 6, fecha = hoy.plusMonths(4))
        val aprobadaPasada = Datos.reserva(id = 7, fecha = hoy.minusYears(1))
        val rechazadaHoy = Datos.reserva(id = 8, fecha = hoy, estado = "RECHAZADA")
        coEvery { reservas.todas(null) } returns Resultado.Exito(
            listOf(pendienteLejana, aprobada4, pendienteHoy, aprobada3, aprobadaHoy, aprobadaPasada, aprobada1, rechazadaHoy)
        )
        val vm = crear()

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        val inicio = (vm.estado.value as EstadoUi.Listo).datos
        assertEquals(listOf(pendienteHoy, pendienteLejana), inicio.pendientes)
        assertEquals(2, inicio.reservasDelMes)          // pendienteHoy + aprobadaHoy
        assertEquals(listOf(aprobadaHoy, aprobada1, aprobada3), inicio.proximas)
    }

    @Test
    fun `error al cargar el inicio muestra el error`() = runTest {
        // Preparar
        coEvery { reservas.todas(null) } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = crear()

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.estado.value)
    }

    @Test
    fun `aprobar cambia el estado a APROBADA, avisa y recarga`() = runTest {
        // Preparar
        val r = Datos.reserva(id = 10, estado = "PENDIENTE")
        coEvery { reservas.cambiarEstado(10, "APROBADA", "Dejar limpio") } returns Resultado.Exito(r.copy(estado = "APROBADA"))
        coEvery { reservas.todas(null) } returns Resultado.Exito(emptyList())
        val vm = crear()
        vm.aprobar(r)
        assertEquals(Decision(r, aprobar = true), vm.porDecidir.value)

        // Ejecutar
        vm.confirmarDecision("Dejar limpio")
        advanceUntilIdle()

        // Verificar
        assertEquals("Solicitud aprobada", vm.mensaje.value)
        assertNull(vm.porDecidir.value)
        assertFalse(vm.decidiendo.value)
        coVerify(exactly = 1) { reservas.cambiarEstado(10, "APROBADA", "Dejar limpio") }
        coVerify(exactly = 1) { reservas.todas(null) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudesViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val reservas = mockk<ReservaRepository>()

    private fun crear() = SolicitudesViewModel(mockk<Application>(relaxed = true), reservas)

    private val temprana = Datos.reserva(id = 1, fecha = hoy.plusDays(1))
    private val tardia = Datos.reserva(id = 2, fecha = hoy.plusDays(9))

    @Test
    fun `pendientes se listan de la mas proxima a la mas lejana`() = runTest {
        // Preparar
        coEvery { reservas.todas("PENDIENTE") } returns Resultado.Exito(listOf(tardia, temprana))
        val vm = crear()

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(listOf(temprana, tardia)), vm.estado.value)
    }

    @Test
    fun `error al cargar las solicitudes muestra el error`() = runTest {
        // Preparar
        coEvery { reservas.todas(null) } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = crear()

        // Ejecutar
        vm.elegirFiltro(FiltroSolicitudes.TODAS)
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.estado.value)
    }
}
