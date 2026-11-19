package com.example.appjuntavecinos.ui.viewmodel

import com.example.appjuntavecinos.data.remote.Resultado
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Estado estándar de cualquier pantalla que carga datos del backend.
 * La pantalla hace un "when" y muestra EstadoCarga / EstadoError / el contenido.
 */
sealed interface EstadoUi<out T> {
    data object Cargando : EstadoUi<Nothing>
    data class Listo<T>(val datos: T, val desdeCache: Boolean = false) : EstadoUi<T>
    data class Error(val mensaje: String) : EstadoUi<Nothing>
}

/** Convierte lo que devuelve el repositorio en el estado de la pantalla. */
fun <T> Resultado<T>.aEstadoUi(): EstadoUi<T> = when (this) {
    is Resultado.Exito -> EstadoUi.Listo(datos, desdeCache)
    is Resultado.Error -> EstadoUi.Error(mensaje)
}

/** Los datos si ya cargó; si no, null (útil para no mostrar el spinner al refrescar). */
val <T> EstadoUi<T>.datosOrNull: T? get() = (this as? EstadoUi.Listo)?.datos

/**
 * Hace DOS llamadas al backend EN PARALELO y junta sus datos.
 * Si cualquiera falla, el resultado es ese error.
 */
suspend fun <A, B, R> combinar(
    primera: suspend () -> Resultado<A>,
    segunda: suspend () -> Resultado<B>,
    juntar: (A, B) -> R
): Resultado<R> = coroutineScope {
    val a = async { primera() }
    val b = async { segunda() }
    val ra = a.await()
    val rb = b.await()
    when {
        ra is Resultado.Error -> ra
        rb is Resultado.Error -> rb
        else -> {
            ra as Resultado.Exito; rb as Resultado.Exito
            Resultado.Exito(juntar(ra.datos, rb.datos), desdeCache = ra.desdeCache || rb.desdeCache)
        }
    }
}
