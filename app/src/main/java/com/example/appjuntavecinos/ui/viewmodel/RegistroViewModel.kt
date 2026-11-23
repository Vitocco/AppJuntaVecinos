package com.example.appjuntavecinos.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.RegistroModel
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.RegisterRequest
import com.example.appjuntavecinos.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

// Repositorio y validador de email por constructor (con valor por defecto) para las pruebas unitarias:
// Patterns es de Android y no existe en la JVM de los tests.
class RegistroViewModel(
    private val repositorio: AuthRepository = AuthRepository(RetrofitClient.api),
    private val esEmailValido: (String) -> Boolean = { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
) : ViewModel() {

    private val _formulario = MutableStateFlow(RegistroModel())
    val formulario: StateFlow<RegistroModel> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(RegistroErrores())
    val errores: StateFlow<RegistroErrores> = _errores.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // Error general del backend o de conexión (el 409 de email va bajo el campo correo)
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun actualizarNombre(valor: String) {
        if (valor == _formulario.value.nombre) return   // el teclado re-envió el mismo texto
        _formulario.value = _formulario.value.copy(nombre = valor)
        _errores.value = _errores.value.copy(nombre = null)
    }
    fun actualizarEmail(valor: String) {
        if (valor == _formulario.value.email) return   // el teclado re-envió el mismo texto
        _formulario.value = _formulario.value.copy(email = valor)
        _errores.value = _errores.value.copy(email = null)
    }
    fun actualizarTelefono(valor: String) {
        // solo números y máximo 9 (el "+56" lo agrega la app al enviarlo)
        if (valor.all { it.isDigit() } && valor.length <= 9) {
            _formulario.value = _formulario.value.copy(telefono = valor)
            _errores.value = _errores.value.copy(telefono = null)
        }
    }
    fun actualizarNumeroCasa(valor: String) {
        if (valor.all { it.isDigit() } && valor.length <= 4) {
            _formulario.value = _formulario.value.copy(numeroCasa = valor)
            _errores.value = _errores.value.copy(numeroCasa = null)
        }
    }
    fun actualizarFechaNacimiento(valor: Long?) {
        _formulario.value = _formulario.value.copy(fechaNacimiento = valor)
        _errores.value = _errores.value.copy(fechaNacimiento = null)
    }
    fun actualizarPassword(valor: String) {
        if (valor == _formulario.value.password) return   // el teclado re-envió el mismo texto
        _formulario.value = _formulario.value.copy(password = valor)
        _errores.value = _errores.value.copy(password = null)
    }
    fun actualizarConfirmarPassword(valor: String) {
        if (valor == _formulario.value.confirmarPassword) return   // el teclado re-envió el mismo texto
        _formulario.value = _formulario.value.copy(confirmarPassword = valor)
        _errores.value = _errores.value.copy(confirmarPassword = null)
    }
    fun actualizarAceptaReglamento(valor: Boolean) {
        _formulario.value = _formulario.value.copy(aceptaReglamento = valor)
        _errores.value = _errores.value.copy(aceptaReglamento = null)
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
        } else if (datos.nombre.trim().length < 3) {
            nombreError = "Debe tener al menos 3 caracteres"
        }

        if (datos.email.isBlank()) {
            emailError = "El correo es obligatorio"
        } else if (!esEmailValido(datos.email.trim())) {
            emailError = "Formato inválido. Ej: nombre@correo.com"
        }

        if (datos.telefono.length != 9) {
            telefonoError = "Deben ser 9 dígitos"
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

        // Regla solo de la app (el backend no la conoce)
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

    // 1° valida, 2° POST api/auth/register, 3° si responde OK queda con sesión iniciada (VECINO).
    // 409 = el correo ya existe: se muestra bajo el campo correo.
    fun guardar(onExito: () -> Unit) {
        if (_cargando.value || !validarFormulario()) return
        val d = _formulario.value

        val solicitud = RegisterRequest(
            nombre = d.nombre.trim(),
            email = d.email.trim(),
            telefono = "+56" + d.telefono,                       // 912345678 -> +56912345678
            numeroCasa = d.numeroCasa,
            fechaNacimiento = fechaParaApi(d.fechaNacimiento!!), // "1990-04-12"
            password = d.password,
            recibirAvisos = d.recibirAvisos
        )

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null
            when (val r = repositorio.registrar(solicitud)) {
                is Resultado.Exito -> onExito()
                is Resultado.Error ->
                    if (r.codigo == 409) _errores.value = _errores.value.copy(email = r.mensaje)
                    else _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    // El DatePicker entrega milisegundos en UTC -> "yyyy-MM-dd" (también en UTC para no correr el día)
    private fun fechaParaApi(utcMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(utcMillis))
}
