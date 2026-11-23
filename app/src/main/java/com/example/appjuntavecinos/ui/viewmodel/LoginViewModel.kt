package com.example.appjuntavecinos.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.LoginModel
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Un mensaje de error por campo. null = sin error (igual que FormularioErrores del profe)
data class LoginErrores(
    val email: String? = null,
    val password: String? = null
)

// Repositorio y validador de email por constructor (con valor por defecto) para las pruebas unitarias:
// Patterns es de Android y no existe en la JVM de los tests.
class LoginViewModel(
    private val repositorio: AuthRepository = AuthRepository(RetrofitClient.api),
    private val esEmailValido: (String) -> Boolean = { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
) : ViewModel() {

    private val _formulario = MutableStateFlow(LoginModel())
    val formulario: StateFlow<LoginModel> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    // true mientras espera la respuesta del backend (el botón muestra un spinner)
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // Mensaje que manda el backend (ej: "Email o contraseña incorrectos") o de conexión
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    // Al escribir se limpia el error de ese campo y el mensaje general.
    // Ojo: el teclado a veces re-envía el MISMO texto (al ocultarse); en ese caso no
    // se borra nada, para que el mensaje del backend no desaparezca solo.
    fun actualizarEmail(valor: String) {
        if (valor == _formulario.value.email) return
        _formulario.value = _formulario.value.copy(email = valor)
        _errores.value = _errores.value.copy(email = null)
        _mensajeError.value = null
    }

    fun actualizarPassword(valor: String) {
        if (valor == _formulario.value.password) return
        _formulario.value = _formulario.value.copy(password = valor)
        _errores.value = _errores.value.copy(password = null)
        _mensajeError.value = null
    }

    fun validarFormulario(): Boolean {
        val datos = _formulario.value
        var emailError: String? = null
        var passwordError: String? = null

        if (datos.email.isBlank()) {
            emailError = "El correo es obligatorio"
        } else if (!esEmailValido(datos.email.trim())) {
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

    // 1° valida el formato, 2° POST api/auth/login, 3° si responde OK la sesión
    // ya quedó guardada (AuthRepository) y se avisa el ROL para navegar.
    fun iniciarSesion(onExito: (String) -> Unit) {
        if (_cargando.value || !validarFormulario()) return
        val datos = _formulario.value

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null
            when (val r = repositorio.login(datos.email, datos.password)) {
                is Resultado.Exito -> onExito(r.datos.rol)
                is Resultado.Error -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }
}
