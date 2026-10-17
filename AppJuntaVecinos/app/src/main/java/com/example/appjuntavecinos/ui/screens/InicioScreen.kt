package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

// Pantalla TEMPORAL: solo para comprobar que la navegación funciona.
// Después la diseñas completa siguiendo la guía (saludo, saldo, mis reservas, barra inferior).
@Composable
fun InicioScreen() {
    // Scaffold: más adelante aquí irá bottomBar = { BarraVecino(...) }
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),          // no quedar debajo de las barras del celular
            contentAlignment = Alignment.Center  // texto al centro de la pantalla
        ) {
            Text("Inicio vecino")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InicioScreenPreview() {
    AppJuntaVecinosTheme { InicioScreen() }
}