package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

@Composable
fun InicioScreen() {
    var selectedItem by remember { mutableStateOf(0) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = {
                        Text("Inicio")
                    }
                )
                NavigationBarItem(
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Reservas"
                        )
                    },
                    label = {
                        Text("Reservas")
                    }
                )
                NavigationBarItem(
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Payments,
                            contentDescription = "Mis pagos"
                        )
                    },
                    label = {
                        Text("Mis pagos")
                    }
                )
                NavigationBarItem(
                    selected = selectedItem == 3,
                    onClick = {
                        selectedItem = 3
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Historial"
                        )
                    },
                    label = {
                        Text("Historial")
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())   // por si no cabe todo
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)  // 20dp entre cada bloque
        ) {
            // ── 1. Saludo ──────────────────────────────
            Column {
                Text("Hola,", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Camila Rojas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // ── 2. Aviso verde "Reserva confirmada" ────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))       // 1° redondear
                    .background(Color(0xFFD7EBDF))         // 2° pintar (si pintas antes de clip, no se redondea)
                    .padding(16.dp),                       // 3° espacio interno
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("Reserva confirmada", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Sábado 18 oct, 15:00–20:00. Te avisaremos el día anterior.", fontSize = 13.sp)
                }
            }

            // ── 3. Tarjeta verde oscura del saldo ──────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Saldo de la junta · Octubre", fontSize = 14.sp, color = Color(0xFFCFE5D8))
                Text("$ 412.300", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(modifier = Modifier.fillMaxWidth()) {
                    // weight(1f) = cada columna toma la mitad del ancho
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ingresos", fontSize = 12.sp, color = Color(0xFFCFE5D8))
                        Text("+ $ 105.000", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gastos", fontSize = 12.sp, color = Color(0xFFCFE5D8))
                        Text("− $ 48.700", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            // ── 4. Botón "Solicitar arriendo" ──────────
            OutlinedButton(
                onClick = { },   // después: navegar al formulario
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Solicitar arriendo de la sede", fontSize = 16.sp)
            }

            // ── 5. Mis reservas ────────────────────────
            Text("Mis reservas", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            TarjetaReserva("Cumpleaños familiar", "Sáb 18 oct · 15:00–20:00", "Confirmada")
            TarjetaReserva("Taller de tejido", "Mié 5 nov · 18:00–20:00", "Pendiente")
        }
    }
}

// Tarjeta blanca de una reserva. La hacemos función porque se repite 2 veces.
@Composable
fun TarjetaReserva(titulo: String, fecha: String, estado: String) {
    // Colores de la etiqueta según el estado
    val fondo = if (estado == "Confirmada") Color(0xFFD7EBDF) else Color(0xFFFCE8D5)
    val texto = if (estado == "Confirmada") Color(0xFF1F5C4A) else Color(0xFF8A3A0A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {   // ocupa todo el espacio que sobra → empuja la etiqueta a la derecha
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(fecha, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            estado,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = texto,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(fondo)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InicioScreenPreview() {
    AppJuntaVecinosTheme { InicioScreen() }
}