package com.example.appjuntavecinos.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Datos del usuario logueado (vienen de la respuesta de login/registro). */
data class Sesion(
    val token: String,
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String          // "VECINO" | "DIRECTIVA" | "TESORERO"
) {
    // "Camila Rojas" -> "Camila" (para "Hola, Camila")
    val primerNombre: String get() = nombre.trim().substringBefore(' ')
}

// Archivo de DataStore donde se guarda la sesión (sobrevive al cerrar la app)
private val Context.almacenSesion: DataStore<Preferences> by preferencesDataStore(name = "sesion")

/**
 * Guarda y expone la sesión actual.
 *  - [sesion]: StateFlow que observa la navegación. Si pasa a null (cerrar sesión o
 *    token vencido), la app vuelve sola al login.
 *  - [cargada]: false mientras se lee DataStore al abrir la app.
 */
object SesionManager {

    private object Claves {
        val TOKEN = stringPreferencesKey("token")
        val ID = longPreferencesKey("id")
        val NOMBRE = stringPreferencesKey("nombre")
        val EMAIL = stringPreferencesKey("email")
        val ROL = stringPreferencesKey("rol")
    }

    private lateinit var almacen: DataStore<Preferences>
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    private val _cargada = MutableStateFlow(false)
    val cargada: StateFlow<Boolean> = _cargada.asStateFlow()

    // Lo lee el AuthInterceptor en cada petición (memoria, sin tocar el disco)
    val token: String? get() = _sesion.value?.token

    /** Se llama una vez al abrir la app (JuntaZarorApp): lee la sesión guardada. */
    fun iniciar(context: Context) {
        almacen = context.applicationContext.almacenSesion
        alcance.launch {
            val p = almacen.data.first()
            val token = p[Claves.TOKEN]
            _sesion.value = if (token != null) {
                Sesion(
                    token = token,
                    id = p[Claves.ID] ?: 0L,
                    nombre = p[Claves.NOMBRE] ?: "",
                    email = p[Claves.EMAIL] ?: "",
                    rol = p[Claves.ROL] ?: ""
                )
            } else null
            _cargada.value = true
        }
    }

    /** Guarda la sesión después de un login o registro exitoso. */
    suspend fun guardar(sesion: Sesion) {
        almacen.edit { p ->
            p[Claves.TOKEN] = sesion.token
            p[Claves.ID] = sesion.id
            p[Claves.NOMBRE] = sesion.nombre
            p[Claves.EMAIL] = sesion.email
            p[Claves.ROL] = sesion.rol
        }
        _sesion.value = sesion
    }

    /** Actualiza solo el nombre (cuando el usuario edita su perfil). */
    suspend fun actualizarNombre(nombre: String) {
        val actual = _sesion.value ?: return
        guardar(actual.copy(nombre = nombre))
    }

    /** Borra todo: botón "Cerrar sesión". */
    suspend fun cerrar() {
        _sesion.value = null
        almacen.edit { it.clear() }
    }

    /** Token vencido (401). Se llama desde el interceptor, que no es una corrutina. */
    fun expirar() {
        if (_sesion.value == null) return
        alcance.launch { cerrar() }
    }
}
