package com.example.appjuntavecinos.ui.viewmodel

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.repository.AuthRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repositorio = mockk<AuthRepository>()

    // En la JVM no existe android.util.Patterns: se usa un validador simple
    private fun crear() = LoginViewModel(repositorio) { it.contains("@") && it.contains(".") }

    private fun LoginViewModel.escribir(email: String, password: String) {
        actualizarEmail(email)
        actualizarPassword(password)
    }

    @Test
    fun `login con credenciales correctas entrega el rol`() = runTest {
        // Preparar
        coEvery { repositorio.login("camila@zaror.cl", "1234") } returns Resultado.Exito(Datos.auth("DIRECTIVA"))
        val vm = crear()
        vm.escribir("camila@zaror.cl", "1234")
        var rol: String? = null

        // Ejecutar
        vm.iniciarSesion { rol = it }
        advanceUntilIdle()

        // Verificar
        assertEquals("DIRECTIVA", rol)
        assertNull(vm.mensajeError.value)
        assertFalse(vm.cargando.value)
        coVerify(exactly = 1) { repositorio.login("camila@zaror.cl", "1234") }
    }

    @Test
    fun `login con clave incorrecta muestra error`() = runTest {
        // Preparar
        coEvery { repositorio.login(any(), any()) } returns Resultado.Error("Email o contraseña incorrectos", 401)
        val vm = crear()
        vm.escribir("camila@zaror.cl", "mala")
        var rol: String? = null

        // Ejecutar
        vm.iniciarSesion { rol = it }
        advanceUntilIdle()

        // Verificar
        assertEquals("Email o contraseña incorrectos", vm.mensajeError.value)
        assertNull(rol)
        assertFalse(vm.cargando.value)
    }

    @Test
    fun `login con campos vacios no llama al repositorio`() = runTest {
        // Preparar
        val vm = crear()

        // Ejecutar
        vm.iniciarSesion { }
        advanceUntilIdle()

        // Verificar
        assertEquals("El correo es obligatorio", vm.errores.value.email)
        assertEquals("La contraseña es obligatoria", vm.errores.value.password)
        coVerify(exactly = 0) { repositorio.login(any(), any()) }
    }

    @Test
    fun `login con email sin formato y clave corta muestra errores y no llama al repositorio`() = runTest {
        // Preparar
        val vm = crear()
        vm.escribir("camila", "123")

        // Ejecutar
        vm.iniciarSesion { }
        advanceUntilIdle()

        // Verificar
        assertEquals("Formato inválido. Ej: nombre@correo.com", vm.errores.value.email)
        assertEquals("Debe tener al menos 4 caracteres", vm.errores.value.password)
        coVerify(exactly = 0) { repositorio.login(any(), any()) }
    }
}
