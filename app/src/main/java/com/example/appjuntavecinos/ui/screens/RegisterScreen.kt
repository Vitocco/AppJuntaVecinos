package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.DosColumnas
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme
import com.example.appjuntavecinos.viewmodel.RegistroViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onVolver: () -> Unit = {},
    onRegistroExitoso: () -> Unit = {},
    viewModel: RegistroViewModel = viewModel()
) {
    val formulario by viewModel.formulario.collectAsState()
    val errores by viewModel.errores.collectAsState()
    var mostrarDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta de vecino") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DosColumnas(
                espacio = 8.dp,
                izquierda = {
                    OutlinedTextField(
                        value = formulario.nombre,
                        onValueChange = { viewModel.actualizarNombre(it) },
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errores.nombre != null,
                        trailingIcon = { IconoError(errores.nombre) },
                        supportingText = { errores.nombre?.let { Text(it) } }
                    )

                    OutlinedTextField(
                        value = formulario.email,
                        onValueChange = { viewModel.actualizarEmail(it) },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        isError = errores.email != null,
                        trailingIcon = { IconoError(errores.email) },
                        supportingText = {
                            if (errores.email != null) Text(errores.email!!) else Text("Ej: nombre@correo.com")
                        }
                    )

                    OutlinedTextField(
                        value = formulario.telefono,
                        onValueChange = { viewModel.actualizarTelefono(it) },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        isError = errores.telefono != null,
                        trailingIcon = { IconoError(errores.telefono) },
                        supportingText = {
                            if (errores.telefono != null) Text(errores.telefono!!) else Text("9 dígitos. Ej: 912345678")
                        }
                    )

                    OutlinedTextField(
                        value = formulario.numeroCasa,
                        onValueChange = { viewModel.actualizarNumeroCasa(it) },
                        label = { Text("N° de casa / depto") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = errores.numeroCasa != null,
                        trailingIcon = { IconoError(errores.numeroCasa) },
                        supportingText = { errores.numeroCasa?.let { Text(it) } }
                    )

                    val fechaTexto = formulario.fechaNacimiento?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date(it))
                    } ?: "Seleccionar fecha"

                    OutlinedTextField(
                        value = fechaTexto,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de nacimiento") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errores.fechaNacimiento != null,
                        trailingIcon = { IconoError(errores.fechaNacimiento) },
                        supportingText = { errores.fechaNacimiento?.let { Text(it) } }
                    )
                    TextButton(onClick = { mostrarDatePicker = true }) {
                        Text("Seleccionar fecha")
                    }

                },
                derecha = {
                    OutlinedTextField(
                        value = formulario.password,
                        onValueChange = { viewModel.actualizarPassword(it) },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = errores.password != null,
                        trailingIcon = { IconoError(errores.password) },
                        supportingText = {
                            if (errores.password != null) Text(errores.password!!) else Text("Mínimo 4 caracteres")
                        }
                    )

                    OutlinedTextField(
                        value = formulario.confirmarPassword,
                        onValueChange = { viewModel.actualizarConfirmarPassword(it) },
                        label = { Text("Confirmar contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = errores.confirmarPassword != null,
                        trailingIcon = { IconoError(errores.confirmarPassword) },
                        supportingText = { errores.confirmarPassword?.let { Text(it) } }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = formulario.aceptaReglamento,
                            onCheckedChange = { viewModel.actualizarAceptaReglamento(it) }
                        )
                        Text("Acepto el reglamento de la junta de vecinos")
                    }
                    if (errores.aceptaReglamento != null) {
                        Text(
                            text = errores.aceptaReglamento!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recibir avisos de mis reservas")
                        Switch(
                            checked = formulario.recibirAvisos,
                            onCheckedChange = { viewModel.actualizarRecibirAvisos(it) }
                        )
                    }

                    Button(
                        onClick = { viewModel.guardar(onExito = onRegistroExitoso) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Crear cuenta")
                    }
                }
            )
        }
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = formulario.fechaNacimiento)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.actualizarFechaNacimiento(datePickerState.selectedDateMillis)
                    mostrarDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun IconoError(error: String?) {
    if (error != null) {
        Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    AppJuntaVecinosTheme { RegisterScreen() }
}