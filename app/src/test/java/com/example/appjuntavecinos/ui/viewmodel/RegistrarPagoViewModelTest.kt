package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.PagoRequest
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.util.Imagenes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Tesorero: registrar el pago manual de una reserva aprobada. */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistrarPagoViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val reservas = mockk<ReservaRepository>()
    private val pagos = mockk<PagoRepository>()
    private val app = mockk<Application>(relaxed = true)
    private val foto = mockk<Uri>()
    private val jpg = byteArrayOf(9, 9)

    @Before
    fun preparar() {
        mockkObject(Imagenes)
        coEvery { Imagenes.comprimirAJpg(app, foto) } returns jpg
    }

    @After
    fun limpiar() = unmockkAll()

    private fun TestScope.crear(): RegistrarPagoViewModel {
        coEvery { reservas.todas("APROBADA") } returns
            Resultado.Exito(listOf(Datos.reserva(id = 4, precio = 25000), Datos.reserva(id = 5, pagada = true)))
        val vm = RegistrarPagoViewModel(app, SavedStateHandle(mapOf("reservaId" to 4L)), reservas, pagos)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `registrar un pago valido envia los datos y la foto`() = runTest {
        // Preparar
        coEvery { pagos.registrarPago(any(), any()) } returns Resultado.Exito(Datos.pago(1))
        val vm = crear()
        vm.actualizarMedio(MedioPago.EFECTIVO)
        vm.actualizarReferencia(" Recibo 45 ")
        vm.actualizarComprobante(foto)

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertTrue(vm.guardado.value)
        assertFalse(vm.guardando.value)
        coVerify(exactly = 1) {
            pagos.registrarPago(PagoRequest(4, 25000, "EFECTIVO", Datos.hoy.toString(), "Recibo 45"), jpg)
        }
    }

    @Test
    fun `sin comprobante ni referencia y con monto 0 muestra los errores`() = runTest {
        // Preparar
        val vm = crear()
        vm.actualizarMonto("0")
        vm.actualizarFecha(Datos.hoy.plusDays(1))

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        val e = vm.errores.value
        assertEquals("El monto debe ser mayor a \$0", e.monto)
        assertEquals("La fecha no puede ser futura", e.fecha)
        assertEquals("Ingresa el N° de transferencia o de recibo", e.referencia)
        assertEquals("Adjunta la foto del comprobante", e.comprobante)
        coVerify(exactly = 0) { pagos.registrarPago(any(), any()) }
    }

    @Test
    fun `error del servidor muestra el mensaje`() = runTest {
        // Preparar
        coEvery { pagos.registrarPago(any(), any()) } returns Resultado.Error("La reserva ya está pagada", 409)
        val vm = crear()
        vm.actualizarReferencia("Transferencia 99")
        vm.actualizarComprobante(foto)

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals("La reserva ya está pagada", vm.mensajeError.value)
        assertFalse(vm.guardado.value)
    }
}
