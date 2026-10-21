package com.example.appjuntavecinos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.CampoHora
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.viewmodel.ReservaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarReservaScreen(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit = {},
    onSolicitudEnviada: () -> Unit = {},
    viewModel: ReservaViewModel = viewModel()
) {
    val form by viewModel.form.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardado by viewModel.guardadoCorrectamente.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val dias = remember { Fechas.proximosDias() }

    // El ViewModel avisa "guardado"; la pantalla avisa a la navegación (no navega ella)
    LaunchedEffect(guardado) {
        if (guardado) { viewModel.consumirGuardado(); onSolicitudEnviada() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Solicitar arriendo") },
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Disponibilidad · ${Fechas.mes(dias.first())}", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dias.forEach { dia ->
                    DiaChip(dia, form.fecha == dia, { viewModel.actualizarFecha(dia) }, Modifier.weight(1f))
                }
            }
            MensajeError(errores.fecha)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoHora("Hora inicio", form.horaInicio, errores.horaInicio != null,
                    { viewModel.actualizarHoraInicio(it) }, Modifier.weight(1f))
                CampoHora("Hora término", form.horaTermino, errores.horaTermino != null,
                    { viewModel.actualizarHoraTermino(it) }, Modifier.weight(1f))
            }
            MensajeError(errores.horaInicio ?: errores.horaTermino)

            OutlinedTextField(
                value = form.motivo,
                onValueChange = { viewModel.actualizarMotivo(it) },
                label = { Text("Motivo del arriendo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errores.motivo != null,
                trailingIcon = {
                    if (errores.motivo != null)
                        Icon(Icons.Filled.ErrorOutline, "Error", tint = MaterialTheme.colorScheme.error)
                    else if (form.motivo.trim().length >= 5)
                        Icon(Icons.Filled.CheckCircle, "Campo válido", tint = MaterialTheme.colorScheme.primary)
                },
                supportingText = { errores.motivo?.let { Text(it) } }
            )

            OutlinedTextField(
                value = form.asistentes,
                onValueChange = { viewModel.actualizarAsistentes(it) },
                label = { Text("N° de asistentes") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = errores.asistentes != null,
                trailingIcon = {
                    if (errores.asistentes != null)
                        Icon(Icons.Filled.ErrorOutline, "Error", tint = MaterialTheme.colorScheme.error)
                },
                supportingText = {
                    Text(errores.asistentes ?: "Máximo ${ReservaViewModel.MAX_ASISTENTES} personas")
                }
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Valor referencial")
                    Text("\$ 35.000", fontWeight = FontWeight.Bold)
                }
            }

            mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { viewModel.enviar() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Enviar solicitud", fontSize = 16.sp) }
        }
    }
}

@Composable
private fun DiaChip(dia: Long, seleccionado: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    // Animación (IE 2.2.2): el fondo cambia suave al elegir el día
    val fondo by animateColorAsState(if (seleccionado) c.primary else c.surface, label = "fondoDia")
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = fondo,
        contentColor = if (seleccionado) c.onPrimary else c.onSurface,
        border = if (seleccionado) null else BorderStroke(1.dp, c.outlineVariant)
    ) {
        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Fechas.nombreDia(dia), fontSize = 11.sp)
            Text(Fechas.numeroDia(dia), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MensajeError(texto: String?) {
    AnimatedVisibility(visible = texto != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            Text(texto ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SolicitarReservaScreenPreview() {
    AppJuntaVecinosTheme {
        SolicitarReservaScreen()
    }
}