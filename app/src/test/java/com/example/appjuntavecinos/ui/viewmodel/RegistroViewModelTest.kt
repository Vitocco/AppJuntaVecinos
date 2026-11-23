package com.example.appjuntavecinos.ui.viewmodel

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.RegisterRequest
import com.example.appjuntavecinos.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repositorio = mockk<AuthRepository>()

    private fun crear() = RegistroViewModel(repositorio) { it.contains("@") && it.contains(".") }

    // El DatePicker entrega milisegundos UTC
    private fun millisUtc(fecha: LocalDate) = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun RegistroViewModel.llenarValido() {
        actualizarNombre("  Camila Rojas ")
        actualizarEmail("camila@zaror.cl")
        actualizarTelefono("912345678")
        actualizarNumeroCasa("12")
        actualizarFechaNacimiento(millisUtc(LocalDate.of(1990, 4, 12)))
        actualizarPassword("1234")
        actualizarConfirmarPassword("1234")
        actualizarAceptaReglamento(true)
    }

    @Test
    fun `registro con datos validos envia la solicitud y avisa el exito`() = runTest {
        // Preparar
        val enviada = slot<RegisterRequest>()
        coEvery { repositorio.registrar(capture(enviada)) } returns Resultado.Exito(Datos.auth())
        val vm = crear()
        vm.llenarValido()
        vm.actualizarRecibirAvisos(false)
        var exito = false

        // Ejecutar
        vm.guardar { exito = true }
        advanceUntilIdle()

        // Verificar
        assertTrue(exito)
        assertEquals(
            RegisterRequest("Camila Rojas", "camila@zaror.cl", "+56912345678", "12", "1990-04-12", "1234", false),
            enviada.captured
        )
        assertFalse(vm.cargando.value)
    }

    @Test
    fun `email repetido 409 muestra el error bajo el campo correo`() = runTest {
        // Preparar
        coEvery { repositorio.registrar(any()) } returns Resultado.Error("El email ya está registrado", 409)
        val vm = crear()
        vm.llenarValido()
        var exito = false

        // Ejecutar
        vm.guardar { exito = true }
        advanceUntilIdle()

        // Verificar
        assertFalse(exito)
        assertEquals("El email ya está registrado", vm.errores.value.email)
        assertNull(vm.mensajeError.value)
    }

    @Test
    fun `contrasenas distintas muestran error y no se registra`() = runTest {
        // Preparar
        val vm = crear()
        vm.llenarValido()
        vm.actualizarConfirmarPassword("4321")

        // Ejecutar
        vm.guardar { }
        advanceUntilIdle()

        // Verificar
        assertEquals("Las contraseñas no coinciden", vm.errores.value.confirmarPassword)
        coVerify(exactly = 0) { repositorio.registrar(any()) }
    }

    @Test
    fun `formulario vacio marca todos los campos obligatorios`() {
        // Preparar
        val vm = crear()

        // Ejecutar
        val valido = vm.validarFormulario()

        // Verificar
        assertFalse(valido)
        val e = vm.errores.value
        assertEquals("El nombre es obligatorio", e.nombre)
        assertEquals("El correo es obligatorio", e.email)
        assertEquals("Deben ser 9 dígitos", e.telefono)
        assertEquals("Debe ingresar un número", e.numeroCasa)
        assertEquals("Debe seleccionar una fecha", e.fechaNacimiento)
        assertEquals("Debe tener al menos 4 caracteres", e.password)
        assertEquals("Debe aceptar el reglamento", e.aceptaReglamento)
    }
}
