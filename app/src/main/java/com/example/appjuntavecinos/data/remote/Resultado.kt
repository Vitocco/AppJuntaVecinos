package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.data.remote.dto.ErrorDto
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Lo que devuelve cada función de los repositorios:
 *  - Exito: los datos. [desdeCache] = true si vinieron de Room porque no hubo conexión.
 *  - Error: el texto para mostrar (el "mensaje" del backend) y el código HTTP
 *    (null = no hubo respuesta, o sea, sin conexión).
 */
sealed class Resultado<out T> {
    data class Exito<T>(val datos: T, val desdeCache: Boolean = false) : Resultado<T>()
    data class Error(val mensaje: String, val codigo: Int? = null) : Resultado<Nothing>() {
        val sinConexion: Boolean get() = codigo == null
    }
}

const val MENSAJE_SIN_CONEXION = "No se pudo conectar con el servidor"

/**
 * Ejecuta una llamada a la API y convierte cualquier falla en Resultado.Error:
 *  - HttpException (400/401/403/404/409): lee {status, error, mensaje} del body.
 *  - IOException: no hubo respuesta (backend apagado, sin internet, timeout).
 */
suspend fun <T> llamarApi(llamada: suspend () -> T): Resultado<T> =
    try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e  // si se cierra la pantalla, la corrutina se cancela: no es un error
    } catch (e: HttpException) {
        Resultado.Error(mensajeDelBackend(e), e.code())
    } catch (e: IOException) {
        Resultado.Error(MENSAJE_SIN_CONEXION, null)
    } catch (e: JsonParseException) {
        Resultado.Error("La respuesta del servidor no tiene el formato esperado", -1)
    }

// Saca el campo "mensaje" del JSON de error; si no viene, un texto según el código.
private fun mensajeDelBackend(e: HttpException): String {
    val cuerpo = e.response()?.errorBody()?.string()
    val mensaje = try {
        RetrofitClient.gson.fromJson(cuerpo, ErrorDto::class.java)?.mensaje
    } catch (_: Exception) {
        null
    }
    return mensaje ?: when (e.code()) {
        401 -> "Tu sesión expiró, vuelve a iniciar sesión"
        403 -> "No tienes permiso para hacer esto"
        404 -> "No se encontró lo solicitado"
        else -> "Error del servidor (${e.code()})"
    }
}
