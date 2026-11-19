package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Multipart
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.PagoDto
import com.example.appjuntavecinos.data.remote.dto.PagoRequest
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarRequest
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarResponse
import com.example.appjuntavecinos.data.remote.llamarApi

// Pagos de reservas (tesorero) y Webpay (vecino, Etapa 4).
class PagoRepository(private val api: ApiService) {

    suspend fun pagos(): Resultado<List<PagoDto>> = llamarApi { api.pagos() }

    /** El tesorero registra un pago manual. El comprobante es OBLIGATORIO. */
    suspend fun registrarPago(datos: PagoRequest, comprobanteJpg: ByteArray): Resultado<PagoDto> =
        llamarApi { api.registrarPago(Multipart.datosJson(datos), Multipart.archivoJpg(comprobanteJpg)) }

    suspend fun iniciarWebpay(reservaId: Long): Resultado<WebpayIniciarResponse> =
        llamarApi { api.iniciarWebpay(WebpayIniciarRequest(reservaId)) }
}
