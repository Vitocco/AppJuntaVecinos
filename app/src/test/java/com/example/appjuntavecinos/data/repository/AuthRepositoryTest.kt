package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.MENSAJE_SIN_CONEXION
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.LoginRequest
import com.example.appjuntavecinos.data.session.Sesion
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthRepositoryTest {

    private val api = mockk<ApiService>()
    private val repositorio = AuthRepository(api)

    @Before
    fun preparar() {
        // SesionManager guarda en DataStore (Android): se simula para no tocar el disco
        mockkObject(SesionManager)
        coEvery { SesionManager.guardar(any()) } just runs
        coEvery { SesionManager.cerrar() } just runs
    }

    @After
    fun limpiar() = unmockkAll()

    @Test
    fun `login devuelve el token y guarda la sesion`() = runTest {
        // Preparar
        coEvery { api.login(LoginRequest("camila@zaror.cl", "1234")) } returns Datos.auth("TESORERO")

        // Ejecutar
        val r = repositorio.login("  camila@zaror.cl ", "1234")    // el email se envía sin espacios

        // Verificar
        assertEquals("jwt-123", (r as Resultado.Exito).datos.token)
        coVerify(exactly = 1) {
            SesionManager.guardar(Sesion("jwt-123", 7, "Camila Rojas", "camila@zaror.cl", "TESORERO"))
        }
    }

    @Test
    fun `login con error de red se propaga como sin conexion y no guarda sesion`() = runTest {
        // Preparar
        coEvery { api.login(any()) } throws IOException("sin red")

        // Ejecutar
        val r = repositorio.login("camila@zaror.cl", "1234")

        // Verificar
        r as Resultado.Error
        assertEquals(MENSAJE_SIN_CONEXION, r.mensaje)
        assertTrue(r.sinConexion)
        coVerify(exactly = 0) { SesionManager.guardar(any()) }
    }
}
