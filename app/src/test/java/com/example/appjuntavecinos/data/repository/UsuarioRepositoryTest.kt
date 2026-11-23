package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.errorHttp
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.ActualizarUsuarioRequest
import com.example.appjuntavecinos.data.session.SesionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UsuarioRepositoryTest {

    private val api = mockk<ApiService>()
    private val repositorio = UsuarioRepository(api)
    private val datos = ActualizarUsuarioRequest("Camila R.", "+56912345678", "12", true)

    @Before
    fun preparar() {
        mockkObject(SesionManager)
        coEvery { SesionManager.actualizarNombre(any()) } just runs
    }

    @After
    fun limpiar() = unmockkAll()

    @Test
    fun `actualizar perfil cambia tambien el nombre de la sesion`() = runTest {
        // Preparar
        coEvery { api.actualizarPerfil(datos) } returns Datos.usuario().copy(nombre = "Camila R.")

        // Ejecutar
        repositorio.actualizarPerfil(datos)

        // Verificar
        coVerify(exactly = 1) { SesionManager.actualizarNombre("Camila R.") }
    }

    @Test
    fun `actualizar perfil con error no toca la sesion`() = runTest {
        // Preparar
        coEvery { api.actualizarPerfil(any()) } throws errorHttp(400, "Teléfono inválido")

        // Ejecutar
        val r = repositorio.actualizarPerfil(datos)

        // Verificar
        assertEquals(Resultado.Error("Teléfono inválido", 400), r)
        coVerify(exactly = 0) { SesionManager.actualizarNombre(any()) }
    }
}
