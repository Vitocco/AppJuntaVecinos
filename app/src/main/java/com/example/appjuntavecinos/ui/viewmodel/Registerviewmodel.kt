package com.example.appjuntavecinos.ui.viewmodel

import android.util.Patterns
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.util.Seguridad
import com.example.appjuntavecinos.data.model.Usuario
import com.example.appjuntavecinos.data.repository.UsuarioRepository
import com.example.appjuntavecinos.data.model.RegistroModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegistroErrores(
    val nombre: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val numeroCasa: String? = null,
    val fechaNacimiento: String? = null,
    val password: String? = null,
    val confirmarPassword: String? = null,
    val aceptaReglamento: String? = null
)

class RegistroViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = UsuarioRepository(AppDatabase.getInstance(application).usuarioDao())

    private val _formulario = MutableStateFlow(RegistroModel())
    val formulario: StateFlow<RegistroModel> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(RegistroErrores())
    val errores: StateFlow<RegistroErrores> = _errores.asStateFlow()

    fun actualizarNombre(valor: String) {
        _formulario.value = _formulario.value.copy(nombre = valor)
    }
    fun actualizarEmail(valor: String) {
        _formulario.value = _formulario.value.copy(email = valor)
    }
    fun actualizarTelefono(valor: String) {
        if (valor.all { it.isDigit() }) {   // solo números, como "cantidad" del profe
            _formulario.value = _formulario.value.copy(telefono = valor)
        }
    }
    fun actualizarNumeroCasa(valor: String) {
        if (valor.all { it.isDigit() }) {
            _formulario.value = _formulario.value.copy(numeroCasa = valor)
        }
    }
    fun actualizarFechaNacimiento(valor: Long?) {
        _formulario.value = _formulario.value.copy(fechaNacimiento = valor)
    }
    fun actualizarPassword(valor: String) {
        _formulario.value = _formulario.value.copy(password = valor)
    }
    fun actualizarConfirmarPassword(valor: String) {
        _formulario.value = _formulario.value.copy(confirmarPassword = valor)
    }
    fun actualizarAceptaReglamento(valor: Boolean) {
        _formulario.value = _formulario.value.copy(aceptaReglamento = valor)
    }
    fun actualizarRecibirAvisos(valor: Boolean) {
        _formulario.value = _formulario.value.copy(recibirAvisos = valor)
    }

    fun validarFormulario(): Boolean {
        val datos = _formulario.value
        var nombreError: String? = null
        var emailError: String? = null
        var telefonoError: String? = null
        var casaError: String? = null
        var fechaError: String? = null
        var passwordError: String? = null
        var confirmarError: String? = null
        var reglamentoError: String? = null

        if (datos.nombre.isBlank()) {
            nombreError = "El nombre es obligatorio"
        } else if (datos.nombre.length < 3) {
            nombreError = "Debe tener al menos 3 caracteres"
        }

        if (datos.email.isBlank()) {
            emailError = "El correo es obligatorio"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(datos.email).matches()) {
            emailError = "Formato inválido. Ej: nombre@correo.com"
        }

        if (datos.telefono.length != 9) {
            telefonoError = "Debe tener 9 dígitos. Ej: 912345678"
        }

        val casa = datos.numeroCasa.toIntOrNull()
        if (casa == null) {
            casaError = "Debe ingresar un número"
        } else if (casa < 1 || casa > 9999) {
            casaError = "Número entre 1 y 9999"
        }

        if (datos.fechaNacimiento == null) {
            fechaError = "Debe seleccionar una fecha"
        } else if (datos.fechaNacimiento > System.currentTimeMillis()) {
            fechaError = "La fecha no puede ser futura"
        }

        if (datos.password.length < 4) {
            passwordError = "Debe tener al menos 4 caracteres"
        }
        if (datos.confirmarPassword != datos.password) {
            confirmarError = "Las contraseñas no coinciden"
        }

        if (!datos.aceptaReglamento) {
            reglamentoError = "Debe aceptar el reglamento"
        }

        val nuevosErrores = RegistroErrores(
            nombre = nombreError,
            email = emailError,
            telefono = telefonoError,
            numeroCasa = casaError,
            fechaNacimiento = fechaError,
            password = passwordError,
            confirmarPassword = confirmarError,
            aceptaReglamento = reglamentoError
        )
        _errores.value = nuevosErrores
        return nuevosErrores == RegistroErrores()
    }

    // 1° valida, 2° revisa que el correo no exista, 3° guarda con la contraseña encriptada
    fun guardar(onExito: () -> Unit) {
        if (!validarFormulario()) return
        val datos = _formulario.value

        viewModelScope.launch {
            if (repositorio.buscarPorEmail(datos.email) != null) {
                _errores.value = RegistroErrores(email = "Este correo ya está registrado")
                return@launch
            }
            repositorio.registrar(
                Usuario(
                    nombre = datos.nombre.trim(),
                    email = datos.email.trim(),
                    telefono = datos.telefono,
                    numeroCasa = datos.numeroCasa.toInt(),
                    fechaNacimiento = datos.fechaNacimiento!!,
                    passwordHash = Seguridad.sha256(datos.password),   // ← aquí se encripta
                    rol = "VECINO",
                    recibirAvisos = datos.recibirAvisos
                )
            )
            onExito()
        }
    }
}
