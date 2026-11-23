package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.apoyo.Datos
import com.example.appjuntavecinos.apoyo.errorHttp
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.PagoRequest
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Test

class PagoRepositoryTest {

    private val api = mockk<ApiService>()
    private val repositorio = PagoRepository(api)

    @Test
    fun `registrar pago envia el comprobante como JPG`() = runTest {
        // Preparar
        val archivo = slot<MultipartBody.Part>()
        coEvery { api.registrarPago(any(), capture(archivo)) } returns Datos.pago(1)

        // Ejecutar
        val r = repositorio.registrarPago(PagoRequest(4, 25000, "EFECTIVO", "2026-11-20", "Recibo 45"), byteArrayOf(1, 2))

        // Verificar
        assertEquals(Resultado.Exito(Datos.pago(1)), r)
        assertEquals("image/jpeg", archivo.captured.body.contentType().toString())
    }

    @Test
    fun `listar pagos con error propaga el mensaje`() = runTest {
        // Preparar
        coEvery { api.pagos() } throws errorHttp(403)

        // Ejecutar
        val r = repositorio.pagos()

        // Verificar
        assertEquals(Resultado.Error("No tienes permiso para hacer esto", 403), r)
    }
}
