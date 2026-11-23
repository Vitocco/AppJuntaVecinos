package com.example.appjuntavecinos.ui.viewmodel

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.ReglaCorrutinas
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.ActualizarUsuarioRequest
import com.example.appjuntavecinos.data.repository.AuthRepository
import com.example.appjuntavecinos.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Perfil (los 3 roles): cargar, editar y cerrar sesión. */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val usuarios = mockk<UsuarioRepository>()
    private val auth = mockk<AuthRepository>()

    private fun TestScope.crear(): PerfilViewModel {
        coEvery { usuarios.obtenerPerfil() } returns Resultado.Exito(Datos.usuario())
        val vm = PerfilViewModel(usuarios, auth)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `guardar un perfil valido envia los datos con +56 y avisa`() = runTest {
        // Preparar
        val actualizado = Datos.usuario().copy(nombre = "Camila R.", telefono = "+56987654321")
        coEvery { usuarios.actualizarPerfil(any()) } returns Resultado.Exito(actualizado)
        val vm = crear()
        vm.actualizarNombre(" Camila R. ")
        vm.actualizarTelefono("987654321")
        vm.actualizarNumeroCasa("15")
        vm.actualizarRecibirAvisos(false)

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        coVerify(exactly = 1) {
            usuarios.actualizarPerfil(ActualizarUsuarioRequest("Camila R.", "+56987654321", "15", false))
        }
        assertEquals("Perfil actualizado", vm.mensaje.value)
        assertEquals(EstadoUi.Listo(actualizado), vm.estado.value)
        assertEquals("987654321", vm.form.value.telefono)
        assertFalse(vm.guardando.value)
    }

    @Test
    fun `datos invalidos muestran los errores y no se guardan`() = runTest {
        // Preparar
        val vm = crear()
        vm.actualizarNombre("Ca")
        vm.actualizarTelefono("9123")
        vm.actualizarNumeroCasa("0")

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals(PerfilErrores("Debe tener al menos 3 caracteres", "Deben ser 9 dígitos", "Número entre 1 y 9999"), vm.errores.value)
        vm.actualizarNombre("")
        vm.actualizarNumeroCasa("")
        vm.guardar()
        assertEquals("El nombre es obligatorio", vm.errores.value.nombre)
        assertEquals("Debe ingresar un número", vm.errores.value.numeroCasa)
        coVerify(exactly = 0) { usuarios.actualizarPerfil(any()) }
    }

    @Test
    fun `error al guardar muestra el mensaje del backend`() = runTest {
        // Preparar
        coEvery { usuarios.actualizarPerfil(any()) } returns Resultado.Error("Error del servidor (500)", 500)
        val vm = crear()

        // Ejecutar
        vm.guardar()
        advanceUntilIdle()

        // Verificar
        assertEquals("Error del servidor (500)", vm.mensaje.value)
        vm.mensajeMostrado()
        assertNull(vm.mensaje.value)
    }
}
