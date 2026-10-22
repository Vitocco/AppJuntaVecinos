package com.example.appjuntavecinos.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.ui.components.DosColumnas
import com.example.appjuntavecinos.ui.components.BottomBar
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

@Composable
fun InicioScreen(
    onSeleccionar: (Int) -> Unit = {},
    onSolicitarArriendo: () -> Unit = {}
) {
    // Pide el permiso de notificaciones al entrar (Android 13+). Mismo patrón que la cámara.
    val permisoNotificacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            permisoNotificacion.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        bottomBar = { BottomBar(seleccionada = 0, onSeleccionar = onSeleccionar) }
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hola,", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Camila Rojas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD7EBDF))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text("Reserva confirmada", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Sábado 18 oct, 15:00–20:00. Te avisaremos el día anterior.", fontSize = 13.sp)
                            }
                        }
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

                            Row(modifier = Modifier.fillMaxWidth()) {
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
                    }

                },
                derecha = {
                    OutlinedButton(
                        onClick = onSolicitarArriendo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Solicitar arriendo de la sede", fontSize = 16.sp)
                    }

                    Text("Mis reservas", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    TarjetaReserva("Cumpleaños familiar", "Sáb 18 oct · 15:00–20:00", "Confirmada")
                    TarjetaReserva("Taller de tejido", "Mié 5 nov · 18:00–20:00", "Pendiente")
                }
            )
        }
    }
}

@Composable
fun TarjetaReserva(titulo: String, fecha: String, estado: String) {
    val fondo = if (estado == "Confirmada") Color(0xFFD7EBDF) else Color(0xFFFCE8D5)
    val texto = if (estado == "Confirmada") Color(0xFF1F5C4A) else Color(0xFF8A3A0A)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(fecha, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = fondo)
            ) {
                Text(
                    estado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = texto,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InicioScreenPreview() {
    AppJuntaVecinosTheme { InicioScreen() }
}