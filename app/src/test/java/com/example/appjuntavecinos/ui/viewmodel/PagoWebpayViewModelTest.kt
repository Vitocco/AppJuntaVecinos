package com.example.appjuntavecinos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarResponse
import com.example.appjuntavecinos.data.repository.PagoRepository
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
class PagoWebpayViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val pagos = mockk<PagoRepository>()

    // Lo mismo que entrega Navigation con la ruta "webpay?url={url}&reserva={reserva}"
    private fun crear() = PagoWebpayViewModel(
        SavedStateHandle(mapOf("url" to "https://webpay.cl/form?token_ws=tok1", "reserva" to 8L)),
        pagos
    )

    @After
    fun limpiar() = unmockkAll()

    @Test
    fun `parte con la URL de la ruta`() {
        // Ejecutar
        val vm = crear()

        // Verificar
        assertEquals("https://webpay.cl/form?token_ws=tok1", vm.url.value)
        assertFalse(vm.reintentando.value)
        assertNull(vm.error.value)
    }

    @Test
    fun `intentar de nuevo pide una transaccion nueva para la misma reserva`() = runTest {
        // Preparar
        mockkObject(Webpay)
        every { Webpay.urlFormulario(any()) } returns "https://webpay.cl/form?token_ws=tok2"
        coEvery { pagos.iniciarWebpay(8) } returns Resultado.Exito(WebpayIniciarResponse("https://webpay.cl/form", "tok2"))
        val vm = crear()

        // Ejecutar
        vm.reintentar()
        advanceUntilIdle()

        // Verificar
        assertEquals("https://webpay.cl/form?token_ws=tok2", vm.url.value)
        assertFalse(vm.reintentando.value)
        assertNull(vm.error.value)
        coVerify(exactly = 1) { pagos.iniciarWebpay(8) }
    }

    @Test
    fun `si falla el reintento muestra el error y mantiene la URL`() = runTest {
        // Preparar
        coEvery { pagos.iniciarWebpay(8) } returns Resultado.Error(MENSAJE_SIN_CONEXION)
        val vm = crear()

        // Ejecutar
        vm.reintentar()
        advanceUntilIdle()

        // Verificar
        assertEquals(MENSAJE_SIN_CONEXION, vm.error.value)
        assertEquals("https://webpay.cl/form?token_ws=tok1", vm.url.value)
        assertFalse(vm.reintentando.value)
    }
}
