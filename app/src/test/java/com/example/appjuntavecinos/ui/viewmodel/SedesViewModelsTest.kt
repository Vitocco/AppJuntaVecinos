package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.Datos.hoy
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.repository.SedeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Calendario de la Directiva: cada sede con "Libre hoy" / "Ocupada HH:mm". */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarioViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val sedes = mockk<SedeRepository>()
    private val reservas = mockk<ReservaRepository>()

    @Test
    fun `cada sede muestra la primera hora ocupada de hoy o libre`() = runTest {
        // Preparar
        coEvery { sedes.obtenerSedes() } returns Resultado.Exito(listOf(Datos.sede(1), Datos.sede(2)))
        coEvery { reservas.todas("APROBADA") } returns Resultado.Exito(
            listOf(
                Datos.reserva(id = 1, sedeId = 1, fecha = hoy, horaInicio = "20:00"),
                Datos.reserva(id = 2, sedeId = 1, fecha = hoy, horaInicio = "10:00"),
                Datos.reserva(id = 3, sedeId = 2, fecha = hoy.plusDays(1), horaInicio = "09:00")
            )
        )
        val vm = CalendarioViewModel(mockk<Application>(relaxed = true), sedes, reservas)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        val lista = (vm.estado.value as EstadoUi.Listo).datos
        assertEquals("10:00", lista[0].ocupadaDesde)
        assertEquals(null, lista[1].ocupadaDesde)
    }

    @Test
    fun `si falla una llamada se muestra el error`() = runTest {
        // Preparar
        coEvery { sedes.obtenerSedes() } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        coEvery { reservas.todas("APROBADA") } returns Resultado.Exito(emptyList())
        val vm = CalendarioViewModel(mockk<Application>(relaxed = true), sedes, reservas)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.estado.value)
    }
}

/** Detalle de una sede: datos, días ocupados del mes y reservas del día. */
@OptIn(ExperimentalCoroutinesApi::class)
class SedeDetalleViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val sedes = mockk<SedeRepository>()
    private val reservas = mockk<ReservaRepository>()

    private fun crear(sedeId: Long) =
        SedeDetalleViewModel(mockk<Application>(relaxed = true), SavedStateHandle(mapOf("sedeId" to sedeId)), sedes, reservas)

    @Test
    fun `al abrir carga la sede, sus reservas vigentes ordenadas y los dias ocupados`() = runTest {
        // Preparar
        val tarde = Datos.reserva(id = 1, sedeId = 2, fecha = hoy.plusDays(4))
        val temprano = Datos.reserva(id = 2, sedeId = 2, fecha = hoy.plusDays(1), estado = "PENDIENTE")
        val rechazada = Datos.reserva(id = 3, sedeId = 2, estado = "RECHAZADA")
        val otraSede = Datos.reserva(id = 4, sedeId = 1)
        val ocupado = Datos.ocupacion(hoy, "18:00", "22:00")
        coEvery { sedes.obtenerSedes() } returns Resultado.Exito(listOf(Datos.sede(1), Datos.sede(2)), desdeCache = true)
        coEvery { reservas.todas(null) } returns Resultado.Exito(listOf(tarde, rechazada, otraSede, temprano))
        coEvery { sedes.disponibilidad(2, any(), any()) } returns Resultado.Exito(listOf(ocupado))

        // Ejecutar
        val vm = crear(2)
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(Datos.sede(2), desdeCache = true), vm.sede.value)
        assertEquals(EstadoUi.Listo(listOf(temprano, tarde)), vm.reservas.value)
        assertEquals(listOf(ocupado), vm.ocupaciones.value)
        coVerify { sedes.disponibilidad(2, vm.mes.value.year, vm.mes.value.monthValue) }
    }

    @Test
    fun `sede que no existe o error de red muestran error`() = runTest {
        // Preparar
        coEvery { sedes.obtenerSedes() } returns Resultado.Exito(listOf(Datos.sede(1)))
        coEvery { reservas.todas(null) } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        coEvery { sedes.disponibilidad(any(), any(), any()) } returns Resultado.Error(MENSAJE_SIN_CONEXION)

        // Ejecutar
        val vm = crear(99)
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error("La sede no existe"), vm.sede.value)
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.reservas.value)
        assertTrue(vm.ocupaciones.value.isEmpty())

        coEvery { sedes.obtenerSedes() } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        vm.cargar()
        advanceUntilIdle()
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.sede.value)
    }
}

/** Detalle de un informe: lo carga al abrir con el id de la ruta. */
@OptIn(ExperimentalCoroutinesApi::class)
class InformeDetalleViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repositorio = mockk<com.example.appjuntavecinos.data.repository.InformeRepository>()

    @Test
    fun `al abrir carga el informe de la ruta`() = runTest {
        // Preparar
        val informe = Datos.informe(5, "2026-10-01")
        coEvery { repositorio.detalle(5) } returns Resultado.Exito(informe)

        // Ejecutar
        val vm = InformeDetalleViewModel(SavedStateHandle(mapOf("id" to 5L)), repositorio)
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(informe), vm.estado.value)
    }

    @Test
    fun `error al cargar el informe muestra el error`() = runTest {
        // Preparar
        coEvery { repositorio.detalle(5) } returns Resultado.Error("No se encontró lo solicitado", 404)

        // Ejecutar
        val vm = InformeDetalleViewModel(SavedStateHandle(mapOf("id" to 5L)), repositorio)
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error("No se encontró lo solicitado"), vm.estado.value)
    }
}
