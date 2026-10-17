package com.example.appjuntavecinos.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.AppDatabase
import com.example.appjuntavecinos.model.Movimiento
import com.example.appjuntavecinos.repository.FinanzasRepository
import com.example.appjuntavecinos.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovimientoForm(
    val tipo: String = "GASTO",
    val categoria: String? = null,
    val monto: String = "",             // String porque viene de un TextField
    val fecha: Long = Fechas.hoy(),
    val detalle: String = "",
    val comprobante: Uri? = null        // foto opcional
)

data class MovimientoErrores(
    val categoria: String? = null,
    val monto: String? = null,
    val fecha: String? = null,
    val detalle: String? = null
)

class MovimientoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val CATEGORIAS_GASTO = listOf("Arriendo", "Luz", "Agua", "Gas", "Mantención", "Otro")
        private val CATEGORIAS_INGRESO = listOf("Arriendo", "Otro")
        fun categoriasDe(tipo: String) = if (tipo == "GASTO") CATEGORIAS_GASTO else CATEGORIAS_INGRESO
    }

    private val repositorio =
        FinanzasRepository(AppDatabase.getInstance(application).finanzasDao())

    private val _form = MutableStateFlow(MovimientoForm())
    val form: StateFlow<MovimientoForm> = _form.asStateFlow()
    private val _errores = MutableStateFlow(MovimientoErrores())
    val errores: StateFlow<MovimientoErrores> = _errores.asStateFlow()
    private val _guardadoCorrectamente = MutableStateFlow(false)
    val guardadoCorrectamente: StateFlow<Boolean> = _guardadoCorrectamente.asStateFlow()
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    // Cada cambio actualiza el formulario y limpia el error de ESE campo
    fun actualizarTipo(v: String) {
        val actual = _form.value
        // si la categoría elegida no existe en el nuevo tipo, se deselecciona
        val categoria = actual.categoria?.takeIf { it in categoriasDe(v) }
        _form.value = actual.copy(tipo = v, categoria = categoria)
        _errores.value = _errores.value.copy(categoria = null)
    }

    fun actualizarCategoria(v: String) {
        _form.value = _form.value.copy(categoria = v)
        _errores.value = _errores.value.copy(categoria = null)
    }

    fun actualizarMonto(v: String) {
        if (v.all { it.isDigit() } && v.length <= 9) {
            _form.value = _form.value.copy(monto = v)
            _errores.value = _errores.value.copy(monto = null)
        }
    }

    fun actualizarFecha(v: Long) {
        _form.value = _form.value.copy(fecha = v)
        _errores.value = _errores.value.copy(fecha = null)
    }

    fun actualizarDetalle(v: String) {
        if (v.length <= 40) {
            _form.value = _form.value.copy(detalle = v)
            _errores.value = _errores.value.copy(detalle = null)
        }
    }

    // Igual que el ejemplo de cámara: solo se guarda la foto si la captura fue correcta
    fun actualizarComprobanteDesdeCamara(uri: Uri?, capturaCorrecta: Boolean) {
        if (capturaCorrecta && uri != null) _form.value = _form.value.copy(comprobante = uri)
    }

    // Reglas de validación (= validarFormulario del profesor)
    private fun validarCampos(): MovimientoErrores {
        val d = _form.value
        val monto = d.monto.toIntOrNull()
        return MovimientoErrores(
            categoria = if (d.categoria == null) "Selecciona una categoría" else null,
            monto = if (monto == null || monto <= 0) "El monto debe ser mayor a \$0" else null,
            fecha = if (d.fecha > Fechas.hoy()) "La fecha no puede ser futura" else null,
            detalle = when {
                d.detalle.isBlank() -> "El detalle es obligatorio."
                d.detalle.trim().length < 3 -> "Mínimo 3 caracteres"
                else -> null
            }
        )
    }

    fun guardar() {
        val errores = validarCampos()
        _errores.value = errores
        if (errores != MovimientoErrores()) return

        val d = _form.value
        val categoria = d.categoria ?: return
        val monto = d.monto.toIntOrNull() ?: return

        viewModelScope.launch {
            try {
                repositorio.guardarMovimiento(
                    Movimiento(
                        fecha = d.fecha,
                        tipo = d.tipo,
                        categoria = categoria,
                        monto = monto,
                        detalle = d.detalle.trim(),
                        comprobante = d.comprobante?.toString()
                    )
                )
                _guardadoCorrectamente.value = true
            } catch (e: Exception) {
                _mensajeError.value = "No se pudo guardar el movimiento."
            }
        }
    }

    fun consumirGuardado() { _guardadoCorrectamente.value = false }
}