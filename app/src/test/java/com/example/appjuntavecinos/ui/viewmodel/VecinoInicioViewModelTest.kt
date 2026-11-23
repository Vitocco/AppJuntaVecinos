package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.Datos.hoy
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.repository.FeriadoRepository
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VecinoInicioViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val feriados = mockk<FeriadoRepository>()
    private val finanzas = mockk<FinanzasRepository>()
    private val reservas = mockk<ReservaRepository>()
    private val pagos = mockk<PagoRepository>()

    private fun crear() = VecinoInicioViewModel(mockk<Application>(relaxed = true), feriados, finanzas, reservas, pagos)

    @Test
    fun `cargar trae el resumen y solo las 2 proximas reservas vigentes`() = runTest {
        // Preparar
        val pasada = Datos.reserva(id = 1, fecha = hoy.minusDays(5))
        val pendiente = Datos.reserva(id = 2, fecha = hoy.plusDays(5), estado = "PENDIENTE")
        val cercana = Datos.reserva(id = 3, fecha = hoy.plusDays(2))
        val cancelada = Datos.reserva(id = 4, fecha = hoy.plusDays(1), estado = "CANCELADA")
        val lejana = Datos.reserva(id = 5, fecha = hoy.plusDays(10))
        coEvery { finanzas.resumen(any(), any()) } returns Resultado.Exito(Datos.resumen)
        coEvery { reservas.misReservas() } returns Resultado.Exito(listOf(pasada, pendiente, cercana, cancelada, lejana))
        val vm = crear()

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(Datos.resumen), vm.resumen.value)
        assertEquals(EstadoUi.Listo(listOf(cercana, pendiente)), vm.proximas.value)
        coVerify { finanzas.resumen(vm.mes.year, vm.mes.monthValue) }
    }

    @Test
    fun `cargar sin conexion muestra el error en ambas secciones`() = runTest {
        // Preparar
        coEvery { finanzas.resumen(any(), any()) } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        coEvery { reservas.misReservas() } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = crear()

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.resumen.value)
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.proximas.value)
    }

    @Test
    fun `feriados deja solo los futuros, ordenados y maximo 5`() = runTest {
        // Preparar
        coEvery { feriados.obtenerFeriados(hoy.year) } returns Resultado.Exito(
            listOf(
                Datos.feriado(hoy.minusDays(10), "Pasado"),
                Datos.feriado(hoy.plusDays(1), "Mañana"),
                Datos.feriado(hoy, "Hoy"),
                Datos.feriado(hoy.plusDays(40), "En 40 días")
            )
        )
        coEvery { feriados.obtenerFeriados(hoy.year + 1) } returns Resultado.Exito(
            listOf(
                Datos.feriado(hoy.plusDays(400), "En 400 días"),
                Datos.feriado(hoy.plusDays(300), "En 300 días"),
                Datos.feriado(hoy.plusDays(500), "En 500 días")
            )
        )
        val vm = crear()

        // Ejecutar
        vm.cargarFeriados()
        advanceUntilIdle()

        // Verificar
        assertEquals(
            listOf("Hoy", "Mañana", "En 40 días", "En 300 días", "En 400 días"),
            vm.feriados.value.map { it.localName }
        )
    }
}
