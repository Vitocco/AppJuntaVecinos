package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.ui.components.DosColumnas
import com.example.appjuntavecinos.ui.components.BottomBarSocio
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

@Composable
fun SocioHomeScreen(
    onSeleccionar: (Int) -> Unit = {},
    onRegistrarMovimiento: () -> Unit = {}
) {
    Scaffold(
        bottomBar = { BottomBarSocio(seleccionada = 0, onSeleccionar = onSeleccionar) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DosColumnas(
                espacio = 20.dp,
                izquierda = {
                    Column {
                        Text("Hola,", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Socio", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Saldo de la junta · Octubre", fontSize = 14.sp, color = Color(0xFFCFE5D8))
                            Text("$ 412.300", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                },
                derecha = {
                    Button(
                        onClick = onRegistrarMovimiento,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Registrar movimiento", fontSize = 16.sp)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SocioHomeScreenPreview() {
    AppJuntaVecinosTheme { SocioHomeScreen() }
}