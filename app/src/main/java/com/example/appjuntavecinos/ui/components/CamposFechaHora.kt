package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.appjuntavecinos.ui.theme.CarbonCampoOn
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.util.Fechas
import java.time.LocalDate

/**
 * Campo de fecha: se ve como CampoTexto y al tocarlo abre el calendario (DatePicker).
 * [hasta] / [desde] limitan las fechas elegibles (ej: un gasto no puede ser futuro).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onFecha: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    desde: LocalDate? = null,
    hasta: LocalDate? = null
) {
    var abierto by rememberSaveable { mutableStateOf(false) }

    CampoTexto(
        etiqueta = etiqueta,
        valor = fecha?.let { Fechas.diaMesAnio(it) } ?: "",
        onValorChange = {},
        placeholder = "Seleccionar fecha",
        icono = Icons.Outlined.CalendarMonth,
        error = error,
        onClick = { abierto = true },
        modifier = modifier
    )

    if (abierto) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = fecha?.let { Fechas.aMillisUtc(it) },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val dia = Fechas.desdeMillisUtc(utcTimeMillis)
                    return (desde == null || !dia.isBefore(desde)) && (hasta == null || !dia.isAfter(hasta))
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { onFecha(Fechas.desdeMillisUtc(it)) }
                    abierto = false
                }) { Text("Aceptar", color = Terracota) }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) { Text("Cancelar", color = GrisCalido) }
            }
        ) { DatePicker(state = estado) }
    }
}

/** Selector de hora ("18:00"): campo que despliega la lista de horas permitidas. */
@Composable
fun SelectorHora(
    etiqueta: String,
    hora: String?,
    opciones: List<String>,
    onHora: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    Box(modifier) {
        CampoTexto(
            etiqueta = etiqueta,
            valor = hora ?: "",
            onValorChange = {},
            placeholder = "--:--",
            icono = Icons.Outlined.Schedule,
            error = error,
            onClick = { abierto = true }
        )
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.background(CarbonCampoOn)
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, color = if (opcion == hora) Terracota else Crema) },
                    onClick = { onHora(opcion); abierto = false }
                )
            }
        }
    }
}
