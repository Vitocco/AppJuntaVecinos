package com.example.appjuntavecinos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.ActualizarUsuarioRequest
import com.example.appjuntavecinos.data.remote.dto.UsuarioDto
import com.example.appjuntavecinos.data.repository.AuthRepository
import com.example.appjuntavecinos.data.repository.UsuarioRepository
import com.example.appjuntavecinos.data.session.SesionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Lo que el usuario puede editar (PUT usuarios/me). El teléfono va SIN el +56.
data class PerfilForm(
    val nombre: String = "",
    val telefono: String = "",
    val numeroCasa: String = "",
    val recibirAvisos: Boolean = true
)

data class PerfilErrores(
    val nombre: String? = null,
    val telefono: String? = null,
    val numeroCasa: String? = null
)

/** Perfil: lo comparten los 3 roles. */
class PerfilViewModel : ViewModel() {
    private val repositorio = UsuarioRepository(RetrofitClient.api)
    private val auth = AuthRepository(RetrofitClient.api)

    val sesion = SesionManager.sesion

    private val _estado = MutableStateFlow<EstadoUi<UsuarioDto>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<UsuarioDto>> = _estado.asStateFlow()

    private val _form = MutableStateFlow(PerfilForm())
    val form: StateFlow<PerfilForm> = _form.asStateFlow()

    private val _errores = MutableStateFlow(PerfilErrores())
    val errores: StateFlow<PerfilErrores> = _errores.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    // Aviso para el Snackbar ("Perfil actualizado" o el mensaje de error del backend)
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = EstadoUi.Cargando
            val r = repositorio.obtenerPerfil()
            if (r is Resultado.Exito) llenarFormulario(r.datos)
            _estado.value = r.aEstadoUi()
        }
    }

    private fun llenarFormulario(u: UsuarioDto) {
        _form.value = PerfilForm(
            nombre = u.nombre,
            // "+56912345678" -> "912345678" para editarlo (el +56 se vuelve a poner al guardar)
            telefono = u.telefono.orEmpty().removePrefix("+56"),
            numeroCasa = u.numeroCasa.orEmpty(),
            recibirAvisos = u.recibirAvisos
        )
    }

    fun actualizarNombre(v: String) {
        if (v == _form.value.nombre) return
        _form.value = _form.value.copy(nombre = v); _errores.value = _errores.value.copy(nombre = null)
    }
    fun actualizarTelefono(v: String) {
        if (v.all { it.isDigit() } && v.length <= 9 && v != _form.value.telefono) {
            _form.value = _form.value.copy(telefono = v); _errores.value = _errores.value.copy(telefono = null)
        }
    }
    fun actualizarNumeroCasa(v: String) {
        if (v.all { it.isDigit() } && v.length <= 4 && v != _form.value.numeroCasa) {
            _form.value = _form.value.copy(numeroCasa = v); _errores.value = _errores.value.copy(numeroCasa = null)
        }
    }
    fun actualizarRecibirAvisos(v: Boolean) { _form.value = _form.value.copy(recibirAvisos = v) }

    // Mismas reglas que el registro
    private fun validar(): Boolean {
        val f = _form.value
        val casa = f.numeroCasa.toIntOrNull()
        val e = PerfilErrores(
            nombre = when {
                f.nombre.isBlank() -> "El nombre es obligatorio"
                f.nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
                else -> null
            },
            telefono = if (f.telefono.length != 9) "Deben ser 9 dígitos" else null,
            numeroCasa = when {
                casa == null -> "Debe ingresar un número"
                casa !in 1..9999 -> "Número entre 1 y 9999"
                else -> null
            }
        )
        _errores.value = e
        return e == PerfilErrores()
    }

    fun guardar() {
        if (_guardando.value || !validar()) return
        val f = _form.value
        viewModelScope.launch {
            _guardando.value = true
            val r = repositorio.actualizarPerfil(
                ActualizarUsuarioRequest(f.nombre.trim(), "+56" + f.telefono, f.numeroCasa, f.recibirAvisos)
            )
            when (r) {
                is Resultado.Exito -> { _estado.value = EstadoUi.Listo(r.datos); llenarFormulario(r.datos); _mensaje.value = "Perfil actualizado" }
                is Resultado.Error -> _mensaje.value = r.mensaje
            }
            _guardando.value = false
        }
    }

    fun mensajeMostrado() { _mensaje.value = null }

    fun cerrarSesion() {
        viewModelScope.launch { auth.cerrarSesion() }   // la navegación vuelve sola al login
    }
}
