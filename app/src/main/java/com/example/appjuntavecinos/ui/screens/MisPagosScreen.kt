package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.components.BottomBar
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

@Composable
fun MisPagosScreen(
    onSeleccionar: (Int) -> Unit = {}
) {
    Scaffold(
        bottomBar = { BottomBar(seleccionada = 2, onSeleccionar = onSeleccionar) }
    ) { innerPadding ->
        Text(
            "Mis pagos: próximamente",
            modifier = Modifier.padding(innerPadding).padding(20.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MisPagosScreenPreview() {
    AppJuntaVecinosTheme { MisPagosScreen() }
}