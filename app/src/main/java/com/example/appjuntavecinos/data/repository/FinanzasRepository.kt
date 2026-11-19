package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Multipart
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.CategoriaDto
import com.example.appjuntavecinos.data.remote.dto.MovimientoDto
import com.example.appjuntavecinos.data.remote.dto.MovimientoRequest
import com.example.appjuntavecinos.data.remote.dto.ResumenDto
import com.example.appjuntavecinos.data.remote.llamarApi

// Resumen del mes, categorías y movimientos (ingresos / gastos).
class FinanzasRepository(private val api: ApiService) {

    suspend fun resumen(anio: Int, mes: Int): Resultado<ResumenDto> =
        llamarApi { api.resumen(anio, mes) }

    suspend fun categorias(): Resultado<List<CategoriaDto>> =
        llamarApi { api.categorias() }

    suspend fun movimientos(anio: Int, mes: Int): Resultado<List<MovimientoDto>> =
        llamarApi { api.movimientos(anio, mes) }

    /** Crea el movimiento. [comprobanteJpg] = bytes de la foto (opcional). */
    suspend fun crearMovimiento(datos: MovimientoRequest, comprobanteJpg: ByteArray?): Resultado<MovimientoDto> =
        llamarApi {
            api.crearMovimiento(
                datos = Multipart.datosJson(datos),
                archivo = comprobanteJpg?.let { Multipart.archivoJpg(it) }
            )
        }

    suspend fun editarMovimiento(id: Long, datos: MovimientoRequest): Resultado<MovimientoDto> =
        llamarApi { api.editarMovimiento(id, datos) }

    suspend fun anularMovimiento(id: Long): Resultado<MovimientoDto> =
        llamarApi { api.anularMovimiento(id) }
}
