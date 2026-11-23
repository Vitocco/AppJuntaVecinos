package com.example.appjuntavecinos.ui.viewmodel

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.CrearInformeRequest
import com.example.appjuntavecinos.data.repository.InformeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** Tesorero: crear un informe (rendición) de un período. */
@OptIn(ExperimentalCoroutinesApi::class)
class NuevoInformeViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repositorio = mockk<InformeRepository>()

    @Test
    fun `informe valido se crea con el periodo elegido`() = runTest {
        // Preparar
        coEvery { repositorio.crear(any()) } returns Resultado.Exito(Datos.informe(1, "2026-10-01"))
        val vm = NuevoInformeViewModel(repositorio)
        vm.actualizarTitulo(" Rendición octubre ")
        vm.actualizarInicio(LocalDate.of(2026, 10, 1))
        vm.actualizarFin(LocalDate.of(2026, 10, 31))
        vm.actualizarObservacion(" Sin novedades ")
        vm.actualizarPublicado(true)

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertTrue(vm.guardado.value)
        assertFalse(vm.guardando.value)
        coVerify(exactly = 1) {
            repositorio.crear(CrearInformeRequest("Rendición octubre", "2026-10-01", "2026-10-31", "Sin novedades", true))
        }
    }

    @Test
    fun `titulo vacio o corto y fin antes del inicio muestran error`() = runTest {
        // Preparar
        val vm = NuevoInformeViewModel(repositorio)
        vm.actualizarTitulo("")
        vm.actualizarInicio(LocalDate.of(2026, 10, 10))
        vm.actualizarFin(LocalDate.of(2026, 10, 1))

        // Ejecutar
        vm.guardar()
        val sinTitulo = vm.errores.value.titulo
        vm.actualizarTitulo("Rend")
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals("El título es obligatorio", sinTitulo)
        assertEquals("Mínimo 5 caracteres", vm.errores.value.titulo)
        assertEquals("Debe ser igual o posterior al inicio", vm.errores.value.fin)
        coVerify(exactly = 0) { repositorio.crear(any()) }
    }

    @Test
    fun `error del servidor muestra el mensaje`() = runTest {
        // Preparar
        coEvery { repositorio.crear(any()) } returns Resultado.Error("Ya existe un informe para ese período", 409)
        val vm = NuevoInformeViewModel(repositorio)

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals("Ya existe un informe para ese período", vm.mensajeError.value)
        assertFalse(vm.guardado.value)
    }
}
