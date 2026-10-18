package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.appjuntavecinos.util.Fechas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoHora(
    etiqueta: String,
    hora: Int?,
    hayError: Boolean,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = hora?.let { Fechas.hora(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = hayError,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            (8..23).forEach { h ->
                DropdownMenuItem(
                    text = { Text(Fechas.hora(h)) },
                    onClick = { onSeleccion(h); expandido = false }
                )
            }
        }
    }
}