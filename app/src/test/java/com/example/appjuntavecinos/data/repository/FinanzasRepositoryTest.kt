package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.errorHttp
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.MovimientoRequest
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanzasRepositoryTest {

    private val api = mockk<ApiService>()
    private val repositorio = FinanzasRepository(api)
    private val datos = MovimientoRequest("GASTO", 3, "Ampolletas", 15000, "2026-11-20")

    private fun RequestBody.texto() = Buffer().also { writeTo(it) }.readUtf8()

    @Test
    fun `crear movimiento con foto envia el JSON y la parte archivo`() = runTest {
        // Preparar
        val json = slot<RequestBody>()
        val archivo = slot<MultipartBody.Part>()
        coEvery { api.crearMovimiento(capture(json), capture(archivo)) } returns Datos.movimiento(1)

        // Ejecutar
        val r = repositorio.crearMovimiento(datos, byteArrayOf(1, 2, 3))

        // Verificar
        assertTrue(r is Resultado.Exito)
        assertTrue(json.captured.texto().contains("\"descripcion\":\"Ampolletas\""))
        assertEquals("image/jpeg", archivo.captured.body.contentType().toString())
        assertEquals(3L, archivo.captured.body.contentLength())
    }

    @Test
    fun `resumen, categorias, movimientos, editar y anular devuelven lo de la API`() = runTest {
        // Preparar
        coEvery { api.resumen(2026, 11) } returns Datos.resumen
        coEvery { api.categorias() } returns listOf(Datos.categoria())
        coEvery { api.movimientos(2026, 11) } returns listOf(Datos.movimiento(1))
        coEvery { api.editarMovimiento(1, datos) } returns Datos.movimiento(1)
        coEvery { api.anularMovimiento(1) } throws errorHttp(409, "El movimiento ya está anulado")

        // Ejecutar y verificar
        assertEquals(Resultado.Exito(Datos.resumen), repositorio.resumen(2026, 11))
        assertEquals(Resultado.Exito(listOf(Datos.categoria())), repositorio.categorias())
        assertEquals(1, (repositorio.movimientos(2026, 11) as Resultado.Exito).datos.size)
        assertNotNull((repositorio.editarMovimiento(1, datos) as Resultado.Exito).datos)
        assertEquals(Resultado.Error("El movimiento ya está anulado", 409), repositorio.anularMovimiento(1))
    }
}
