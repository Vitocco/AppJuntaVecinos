package com.example.appjuntavecinos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appjuntavecinos.data.CamaraUriFactory
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.viewmodel.MovimientoViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegistrarMovimientoScreen(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit = {},
    onMovimientoGuardado: () -> Unit = {},
    viewModel: MovimientoViewModel = viewModel()
) {
    val context = LocalContext.current
    val form by viewModel.form.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardado by viewModel.guardadoCorrectamente.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    val tipos = listOf("INGRESO" to "Ingreso", "GASTO" to "Gasto")
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    // El ViewModel avisa "guardado"; la pantalla avisa a la navegación (no navega ella)
    LaunchedEffect(guardado) {
        if (guardado) { viewModel.consumirGuardado(); onMovimientoGuardado() }
    }

    // ---------- Cámara (mismo flujo del ejemplo del profesor) ----------
    var uriPendienteCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        viewModel.actualizarComprobanteDesdeCamara(uriPendienteCamara, exito)
        if (!exito) uriPendienteCamara = null
    }

    fun abrirCamara() {
        try {
            val uri = CamaraUriFactory.crearUri(context)
            uriPendienteCamara = uri
            camaraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(context, "Debes conceder el permiso de cámara en Ajustes.", Toast.LENGTH_LONG).show()
    }

    fun tomarFotografia() {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (permisoConcedido) abrirCamara() else permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
    }

    // ---------- Calendario del campo Fecha ----------
    if (mostrarCalendario) {
        val estadoCalendario = rememberDatePickerState(
            initialSelectedDateMillis = Fechas.aMillisUtc(form.fecha),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= Fechas.aMillisUtc(Fechas.hoy())
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoCalendario.selectedDateMillis?.let {
                        viewModel.actualizarFecha(Fechas.desdeMillisUtc(it))
                    }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = estadoCalendario) }
    }

    // ---------- Estructura de la pantalla ----------
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SOCIO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Registrar movimiento", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                AnimatedVisibility(visible = mensajeError != null) {
                    Text(
                        mensajeError ?: "", color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = { viewModel.guardar() },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Guardar movimiento", fontSize = 16.sp) }
            }
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
            // Ingreso / Gasto
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                tipos.forEachIndexed { index, (valor, texto) ->
                    SegmentedButton(
                        selected = form.tipo == valor,
                        onClick = { viewModel.actualizarTipo(valor) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tipos.size)
                    ) { Text(texto) }
                }
            }

            // Categoría
            Text(
                "Categoría", fontWeight = FontWeight.SemiBold,
                color = if (errores.categoria != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MovimientoViewModel.categoriasDe(form.tipo).forEach { cat ->
                    CategoriaChip(cat, form.categoria == cat) { viewModel.actualizarCategoria(cat) }
                }
            }
            MensajeErrorCampo(errores.categoria)

            // Monto
            OutlinedTextField(
                value = form.monto,
                onValueChange = { viewModel.actualizarMonto(it) },
                label = { Text("Monto (CLP)") },
                placeholder = { Text("0") },
                prefix = { Text("\$ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = errores.monto != null,
                trailingIcon = {
                    if (errores.monto != null)
                        Icon(Icons.Filled.ErrorOutline, "Error", tint = MaterialTheme.colorScheme.error)
                },
                supportingText = { errores.monto?.let { Text(it) } }
            )

            // Fecha (abre el calendario) + Detalle
            val interaccionFecha = remember { MutableInteractionSource() }
            LaunchedEffect(interaccionFecha) {
                interaccionFecha.interactions.collect {
                    if (it is PressInteraction.Release) mostrarCalendario = true
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = Fechas.fechaCompleta(form.fecha),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    singleLine = true,
                    interactionSource = interaccionFecha,
                    trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Elegir fecha") },
                    isError = errores.fecha != null,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = form.detalle,
                    onValueChange = { viewModel.actualizarDetalle(it) },
                    label = { Text("Detalle") },
                    singleLine = true,
                    isError = errores.detalle != null,
                    trailingIcon = {
                        if (errores.detalle != null)
                            Icon(Icons.Filled.ErrorOutline, "Error", tint = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            MensajeErrorCampo(errores.fecha ?: errores.detalle)

            // Comprobante (cámara)
            Text(
                "Comprobante", fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.width(88.dp).height(110.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (form.comprobante != null) {
                            AsyncImage(
                                model = form.comprobante,
                                contentDescription = "Foto del comprobante",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Image, contentDescription = "Sin comprobante",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { tomarFotografia() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Cámara")
                        Text(if (form.comprobante == null) "  Tomar foto" else "  Tomar otra foto")
                    }
                    Text(
                        "Usa la cámara del teléfono. Se pedirá permiso la primera vez.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriaChip(texto: String, seleccionada: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    // Animación: el fondo cambia suave al elegir la categoría
    val fondo by animateColorAsState(if (seleccionada) c.primary else c.surface, label = "fondoCategoria")
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = fondo,
        contentColor = if (seleccionada) c.onPrimary else c.onSurface,
        border = if (seleccionada) null else BorderStroke(1.dp, c.outlineVariant)
    ) {
        Text(
            texto, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MensajeErrorCampo(texto: String?) {
    AnimatedVisibility(visible = texto != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            Text(texto ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
    }
}