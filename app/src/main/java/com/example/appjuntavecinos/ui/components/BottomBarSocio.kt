package com.example.appjuntavecinos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomBarSocio(
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
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Gestión") },
            label = { Text("Gestión") }
        )
        NavigationBarItem(
            selected = seleccionada == 2,
            onClick = { onSeleccionar(2) },
            icon = { Icon(Icons.Filled.Add, contentDescription = "Movimiento") },
            label = { Text("Movimiento") }
        )
        NavigationBarItem(
            selected = seleccionada == 3,
            onClick = { onSeleccionar(3) },
            icon = { Icon(Icons.Filled.BarChart, contentDescription = "Reportes") },
            label = { Text("Reportes") }
        )
    }
}