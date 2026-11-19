package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.BotonPrincipal
import com.example.appjuntavecinos.ui.components.BotonVolver
import com.example.appjuntavecinos.ui.components.CampoTexto
import com.example.appjuntavecinos.ui.components.CasillaVerificacion
import com.example.appjuntavecinos.ui.components.DosColumnas
import com.example.appjuntavecinos.ui.components.FondoZaror
import com.example.appjuntavecinos.ui.components.HojaReglamento
import com.example.appjuntavecinos.ui.components.MensajeError
import com.example.appjuntavecinos.ui.components.MensajeExito
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.viewmodel.RegistroViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Registro (diseño 2, "Crear cuenta."): mismo estilo del login, campos en pares
 * (teléfono | casa, contraseña | confirmar) y el error de cada campo debajo de él.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onVolver: () -> Unit = {},
    onRegistroExitoso: () -> Unit = {},
    viewModel: RegistroViewModel = viewModel()
) {
    val formulario by viewModel.formulario.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    var mostrarDatePicker by rememberSaveable { mutableStateOf(false) }
    var mostrarReglamento by rememberSaveable { mutableStateOf(false) }

    // Hoja inferior con el reglamento. "Entendido" la cierra y marca la casilla.
    if (mostrarReglamento) {
        HojaReglamento(
            onEntendido = {
                viewModel.actualizarAceptaReglamento(true)
                mostrarReglamento = false
            },
            onCerrar = { mostrarReglamento = false }
        )
    }

    var registrado by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    LaunchedEffect(registrado) {
        if (registrado) {
            scroll.animateScrollTo(0)   // sube para que se vea el cartel
            delay(1500)
            onRegistroExitoso()
        }
    }

    FondoZaror {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BotonVolver(onClick = onVolver)
            TextoConPunto("Crear cuenta", estilo = MaterialTheme.typography.displaySmall)

            MensajeExito(visible = registrado, texto = "¡Cuenta creada con éxito!")

            DosColumnas(
                espacio = 14.dp,
                izquierda = {
                    CampoTexto(
                        etiqueta = "Nombre completo",
                        valor = formulario.nombre,
                        onValorChange = viewModel::actualizarNombre,
                        placeholder = "Ej: Camila Rojas",
                        icono = Icons.Outlined.Person,
                        error = errores.nombre
                    )
                    CampoTexto(
                        etiqueta = "Correo electrónico",
                        valor = formulario.email,
                        onValorChange = viewModel::actualizarEmail,
                        placeholder = "nombre@correo.com",
                        icono = Icons.Outlined.Email,
                        tipoTeclado = KeyboardType.Email,
                        error = errores.email
                    )
                    // Par: teléfono | n° de casa
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        CampoTexto(
                            etiqueta = "Teléfono",
                            valor = formulario.telefono,
                            onValorChange = viewModel::actualizarTelefono,
                            placeholder = "9 1234 5678",
                            prefijo = "+56 ",
                            tipoTeclado = KeyboardType.Phone,
                            error = errores.telefono,
                            modifier = Modifier.weight(1f)
                        )
                        CampoTexto(
                            etiqueta = "N° casa / depto",
                            valor = formulario.numeroCasa,
                            onValorChange = viewModel::actualizarNumeroCasa,
                            placeholder = "Ej: 24",
                            tipoTeclado = KeyboardType.Number,
                            error = errores.numeroCasa,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    CampoTexto(
                        etiqueta = "Fecha de nacimiento",
                        valor = formulario.fechaNacimiento?.let { formatearFechaUtc(it) } ?: "",
                        onValorChange = {},
                        placeholder = "Seleccionar fecha",
                        icono = Icons.Outlined.CalendarMonth,
                        error = errores.fechaNacimiento,
                        onClick = { mostrarDatePicker = true }
                    )
                },
                derecha = {
                    // Par: contraseña | confirmar
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        CampoTexto(
                            etiqueta = "Contraseña",
                            valor = formulario.password,
                            onValorChange = viewModel::actualizarPassword,
                            placeholder = "••••",
                            esPassword = true,
                            error = errores.password,
                            modifier = Modifier.weight(1f)
                        )
                        CampoTexto(
                            etiqueta = "Confirmar",
                            valor = formulario.confirmarPassword,
                            onValorChange = viewModel::actualizarConfirmarPassword,
                            placeholder = "••••",
                            esPassword = true,
                            error = errores.confirmarPassword,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Column {
                        CasillaVerificacion(
                            texto = "Recibir avisos de mis reservas",
                            marcada = formulario.recibirAvisos,
                            onCambio = viewModel::actualizarRecibirAvisos
                        )
                        // "reglamento de uso de sedes" va en terracota y abre el reglamento
                        CasillaVerificacion(
                            texto = "Acepto el ",
                            textoEnlace = "reglamento de uso de sedes",
                            onEnlace = { mostrarReglamento = true },
                            marcada = formulario.aceptaReglamento,
                            onCambio = viewModel::actualizarAceptaReglamento,
                            error = errores.aceptaReglamento
                        )
                    }

                    MensajeError(mensajeError)
                    BotonPrincipal(
                        texto = "Crear cuenta",
                        cargando = cargando || registrado,
                        onClick = { viewModel.guardar(onExito = { registrado = true }) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            )
        }
    }

    // Calendario de la fecha de nacimiento (no deja elegir fechas futuras)
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formulario.fechaNacimiento,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.actualizarFechaNacimiento(datePickerState.selectedDateMillis)
                    mostrarDatePicker = false
                }) { Text("Aceptar", color = Terracota) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar", color = GrisCalido) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// El DatePicker entrega la fecha en UTC: se formatea en UTC para no correrla un día.
private fun formatearFechaUtc(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-CL")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis)).replace(".", "")

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    AppJuntaVecinosTheme { RegistroScreen() }
}
