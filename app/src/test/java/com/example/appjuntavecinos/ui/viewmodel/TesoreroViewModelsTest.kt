package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.Datos.hoy
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.InformeRepository
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Inicio del tesorero: resumen, 3 pagos y 3 movimientos. */
@OptIn(ExperimentalCoroutinesApi::class)
class TesoreroInicioViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val finanzas = mockk<FinanzasRepository>()
    private val pagos = mockk<PagoRepository>()
    private val reservas = mockk<ReservaRepository>()

    @Test
    fun `cargar trae el resumen, los 3 primeros pagos y los 3 movimientos mas nuevos`() = runTest {
        // Preparar
        val porPagar1 = Datos.reserva(id = 1, fecha = hoy.plusDays(1))
        val porPagar2 = Datos.reserva(id = 2, fecha = hoy.plusDays(2))
        val pagada = Datos.reserva(id = 3, pagada = true)
        val pagoNuevo = Datos.pago(10, fechaPago = "2026-11-20")
        val pagoViejo = Datos.pago(11, fechaPago = "2026-10-01")
        val movs = (1L..4L).map { Datos.movimiento(it, hoy.minusDays(it)) }
        coEvery { finanzas.resumen(any(), any()) } returns Resultado.Exito(Datos.resumen)
        coEvery { pagos.pagos() } returns Resultado.Exito(listOf(pagoViejo, pagoNuevo))
        coEvery { reservas.todas("APROBADA") } returns Resultado.Exito(listOf(porPagar2, pagada, porPagar1))
        coEvery { finanzas.movimientos(any(), any()) } returns Resultado.Exito(movs.reversed())
        val vm = TesoreroInicioViewModel(mockk<Application>(relaxed = true), finanzas, pagos, reservas)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(Datos.resumen), vm.resumen.value)
        assertEquals(
            listOf(ItemPago.PorPagar(porPagar1), ItemPago.PorPagar(porPagar2), ItemPago.Pagado(pagoNuevo)),
            (vm.pagos.value as EstadoUi.Listo).datos
        )
        assertEquals(listOf(1L, 2L, 3L), (vm.movimientos.value as EstadoUi.Listo).datos.map { it.id })
    }

    @Test
    fun `si falla una de las dos llamadas de pagos se muestra el error`() = runTest {
        // Preparar
        coEvery { finanzas.resumen(any(), any()) } returns Resultado.Exito(Datos.resumen)
        coEvery { pagos.pagos() } returns Resultado.Exito(emptyList())
        coEvery { reservas.todas("APROBADA") } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        coEvery { finanzas.movimientos(any(), any()) } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = TesoreroInicioViewModel(mockk<Application>(relaxed = true), finanzas, pagos, reservas)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.pagos.value)
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.movimientos.value)
    }
}

/** Movimientos del tesorero: lista por mes y anular. */
@OptIn(ExperimentalCoroutinesApi::class)
class MovimientosViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val finanzas = mockk<FinanzasRepository>()

    @Test
    fun `cargar lista los movimientos del mes del mas nuevo al mas viejo`() = runTest {
        // Preparar
        val viejo = Datos.movimiento(1, hoy.minusDays(5))
        val nuevo = Datos.movimiento(2, hoy)
        coEvery { finanzas.movimientos(any(), any()) } returns Resultado.Exito(listOf(viejo, nuevo))
        val vm = MovimientosViewModel(finanzas)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(listOf(nuevo, viejo)), vm.estado.value)
        coVerify { finanzas.movimientos(vm.mes.value.year, vm.mes.value.monthValue) }
        assertEquals(6, vm.meses.size)
    }

    @Test
    fun `anular con error muestra el mensaje y no recarga`() = runTest {
        // Preparar
        val m = Datos.movimiento(5)
        coEvery { finanzas.anularMovimiento(5) } returns Resultado.Error("El movimiento ya está anulado", 409)
        val vm = MovimientosViewModel(finanzas)
        vm.pedirAnular(m)

        // Ejecutar
        vm.confirmarAnular()
        advanceUntilIdle()

        // Verificar
        assertEquals("El movimiento ya está anulado", vm.mensaje.value)
        coVerify(exactly = 0) { finanzas.movimientos(any(), any()) }
        vm.mensajeMostrado()
        assertNull(vm.mensaje.value)
    }
}

/** Pagos del tesorero: junta pagos y reservas aprobadas sin pagar. */
@OptIn(ExperimentalCoroutinesApi::class)
class PagosViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val pagos = mockk<PagoRepository>()
    private val reservas = mockk<ReservaRepository>()

    @Test
    fun `cargar pone primero las por pagar y solo muestra los pagos PAGADO`() = runTest {
        // Preparar
        val porPagar = Datos.reserva(id = 1)
        val pagado = Datos.pago(10)
        val webpayAbandonado = Datos.pago(11, estado = "PENDIENTE")
        coEvery { pagos.pagos() } returns Resultado.Exito(listOf(webpayAbandonado, pagado))
        coEvery { reservas.todas("APROBADA") } returns Resultado.Exito(listOf(porPagar, Datos.reserva(id = 2, pagada = true)))
        val vm = PagosViewModel(mockk<Application>(relaxed = true), pagos, reservas)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(
            EstadoUi.Listo(listOf(ItemPago.PorPagar(porPagar), ItemPago.Pagado(pagado))),
            vm.estado.value
        )
    }

    @Test
    fun `error al cargar los pagos muestra el error y el filtro se puede cambiar`() = runTest {
        // Preparar
        coEvery { pagos.pagos() } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        coEvery { reservas.todas("APROBADA") } returns Resultado.Exito(emptyList())
        val vm = PagosViewModel(mockk<Application>(relaxed = true), pagos, reservas)

        // Ejecutar
        vm.cargar()
        vm.elegirFiltro(FiltroPagos.POR_PAGAR)
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.estado.value)
        assertEquals(FiltroPagos.POR_PAGAR, vm.filtro.value)
    }
}

/** Informes del tesorero (incluye borradores). */
@OptIn(ExperimentalCoroutinesApi::class)
class InformesViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repositorio = mockk<InformeRepository>()

    @Test
    fun `cargar ordena los informes del mas nuevo al mas viejo`() = runTest {
        // Preparar
        val octubre = Datos.informe(1, "2026-10-01")
        val noviembre = Datos.informe(2, "2026-11-01")
        coEvery { repositorio.informes() } returns Resultado.Exito(listOf(octubre, noviembre))
        val vm = InformesViewModel(repositorio)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Listo(listOf(noviembre, octubre)), vm.estado.value)
    }

    @Test
    fun `error al cargar los informes muestra el error`() = runTest {
        // Preparar
        coEvery { repositorio.informes() } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = InformesViewModel(repositorio)

        // Ejecutar
        vm.cargar()
        advanceUntilIdle()

        // Verificar
        assertEquals(EstadoUi.Error(MENSAJE_SIN_CONEXION), vm.estado.value)
    }
}
