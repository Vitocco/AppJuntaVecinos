package com.example.appjuntavecinos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomBar(
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit = {}
) {
    NavigationBar {
        NavigationBarItem(
            selected = seleccionada == 0,
            onClick = { onSeleccionar(0) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = seleccionada == 1,
            onClick = { onSeleccionar(1) },
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Reservas") },
            label = { Text("Reservas") }
        )
        NavigationBarItem(
            selected = seleccionada == 2,
            onClick = { onSeleccionar(2) },
            icon = { Icon(Icons.Filled.Payments, contentDescription = "Mis pagos") },
            label = { Text("Mis pagos") }
        )
        NavigationBarItem(
            selected = seleccionada == 3,
            onClick = { onSeleccionar(3) },
            icon = { Icon(Icons.Filled.History, contentDescription = "Historial") },
            label = { Text("Historial") }
        )
    }
}