package com.example.appjuntavecinos.viewmodel

import android.util.Patterns
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.AppDatabase
import com.example.appjuntavecinos.util.Seguridad
import com.example.appjuntavecinos.repository.UsuarioRepository
import com.example.appjuntavecinos.model.LoginModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Un mensaje de error por campo. null = sin error (igual que FormularioErrores del profe)
data class LoginErrores(
    val email: String? = null,
    val password: String? = null
)

// AndroidViewModel: igual que EstadoViewModel del profe, para poder abrir la base de datos
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = UsuarioRepository(AppDatabase.getInstance(application).usuarioDao())

    init {
        viewModelScope.launch { repositorio.crearSocioDePrueba() }   // socio@zaror.cl / 1234
    }

    private val _formulario = MutableStateFlow(LoginModel())
    val formulario: StateFlow<LoginModel> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    fun actualizarEmail(valor: String) {
        _formulario.value = _formulario.value.copy(email = valor)
    }

    fun actualizarPassword(valor: String) {
        _formulario.value = _formulario.value.copy(password = valor)
    }

    fun actualizarRol(valor: Int) {
        _formulario.value = _formulario.value.copy(rol = valor)
    }

    fun validarFormulario(): Boolean {
        val datos = _formulario.value
        var emailError: String? = null
        var passwordError: String? = null

        if (datos.email.isBlank()) {
            emailError = "El correo es obligatorio"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(datos.email).matches()) {
            emailError = "Formato inválido. Ej: nombre@correo.com"
        }

        if (datos.password.isBlank()) {
            passwordError = "La contraseña es obligatoria"
        } else if (datos.password.length < 4) {
            passwordError = "Debe tener al menos 4 caracteres"
        }

        val nuevosErrores = LoginErrores(email = emailError, password = passwordError)
        _errores.value = nuevosErrores
        return nuevosErrores == LoginErrores()
    }

    // 1° valida el formato, 2° busca en Room, 3° compara el hash de la contraseña
    fun iniciarSesion(onExito: (String) -> Unit) {
        if (!validarFormulario()) return
        val datos = _formulario.value

        viewModelScope.launch {
            val usuario = repositorio.buscarPorEmail(datos.email)
            val rolElegido = if (datos.rol == 0) "VECINO" else "SOCIO"

            if (usuario == null || usuario.passwordHash != Seguridad.sha256(datos.password)) {
                _errores.value = LoginErrores(password = "Correo o contraseña incorrectos")
            } else if (usuario.rol != rolElegido) {
                _errores.value = LoginErrores(email = "Esta cuenta no es de tipo ${rolElegido.lowercase()}")
            } else {
                onExito(rolElegido)
            }
        }
    }
}
