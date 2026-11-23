package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.MovimientoRequest
import com.example.appjuntavecinos.data.repository.FinanzasRepository
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
import org.junit.Rule
import org.junit.Test

/** Tesorero: registrar / editar un movimiento (ingreso o gasto). */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistrarMovimientoViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val finanzas = mockk<FinanzasRepository>()
    private val app = mockk<Application>(relaxed = true)

    private fun TestScope.crear(vararg ruta: Pair<String, Any?>): RegistrarMovimientoViewModel {
        coEvery { finanzas.categorias() } returns Resultado.Exito(listOf(Datos.categoria(3)))
        val vm = RegistrarMovimientoViewModel(app, SavedStateHandle(mapOf(*ruta)), finanzas)
        advanceUntilIdle()
        return vm
    }

    private fun RegistrarMovimientoViewModel.llenarGasto() {
        actualizarCategoria(3)
        actualizarMonto("15000")
        actualizarDetalle(" Ampolletas ")
    }

    @After
    fun limpiar() = unmockkAll()

    @Test
    fun `registrar gasto con foto sube el comprobante comprimido`() = runTest {
        // Preparar
        val foto = mockk<Uri>()
        val jpg = byteArrayOf(1, 2, 3)
        mockkObject(Imagenes)
        coEvery { Imagenes.comprimirAJpg(app, foto) } returns jpg
        coEvery { finanzas.crearMovimiento(any(), any()) } returns Resultado.Exito(Datos.movimiento(1))
        val vm = crear("tipo" to "GASTO")
        vm.llenarGasto()
        vm.actualizarComprobante(foto)

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertTrue(vm.guardado.value)
        assertFalse(vm.guardando.value)
        coVerify(exactly = 1) {
            finanzas.crearMovimiento(MovimientoRequest("GASTO", 3, "Ampolletas", 15000, Datos.hoy.toString()), jpg)
        }
    }

    @Test
    fun `monto 0 o vacio muestra error y no guarda`() = runTest {
        // Preparar
        val vm = crear("tipo" to "GASTO")
        vm.llenarGasto()

        // Ejecutar
        vm.actualizarMonto("")
        vm.guardar()
        val conVacio = vm.errores.value.monto
        vm.actualizarMonto("0")
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals("El monto debe ser mayor a \$0", conVacio)
        assertEquals("El monto debe ser mayor a \$0", vm.errores.value.monto)
        coVerify(exactly = 0) { finanzas.crearMovimiento(any(), any()) }
    }

    @Test
    fun `sin categoria muestra error y no guarda`() = runTest {
        // Preparar
        val vm = crear("tipo" to "GASTO")
        vm.actualizarMonto("15000")
        vm.actualizarDetalle("Ampolletas")

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals("Selecciona una categoría", vm.errores.value.categoria)
        coVerify(exactly = 0) { finanzas.crearMovimiento(any(), any()) }
    }

    @Test
    fun `error del servidor muestra el mensaje`() = runTest {
        // Preparar
        coEvery { finanzas.crearMovimiento(any(), any()) } returns Resultado.Error("Error del servidor (500)", 500)
        val vm = crear("tipo" to "GASTO")
        vm.llenarGasto()

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals("Error del servidor (500)", vm.mensajeError.value)
        assertFalse(vm.guardado.value)
        assertFalse(vm.guardando.value)
    }
}
