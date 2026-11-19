package com.example.appjuntavecinos.ui.screens.directiva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.DialogoDecision
import com.example.appjuntavecinos.ui.components.EncabezadoHome
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FilaReserva
import com.example.appjuntavecinos.ui.components.MostrarMensaje
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.Tarjeta
import com.example.appjuntavecinos.ui.components.TarjetaSolicitud
import com.example.appjuntavecinos.ui.components.TituloSeccion
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.viewmodel.DirectivaInicioViewModel
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.dia

/**
 * Inicio de la Directiva (diseño 3): contadores "Por revisar" y "Reservas <mes>",
 * solicitudes PENDIENTES con Aprobar/Rechazar y próximas reservas aprobadas.
 */
@Composable
fun DirectivaInicioScreen(
    onVerSolicitudes: () -> Unit,
    viewModel: DirectivaInicioViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val porDecidir by viewModel.porDecidir.collectAsState()
    val decidiendo by viewModel.decidiendo.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    AlVolverAPantalla { viewModel.cargar() }
    MostrarMensaje(mensaje, viewModel::mensajeMostrado)
    porDecidir?.let {
        DialogoDecision(it, decidiendo, viewModel::confirmarDecision, viewModel::descartarDecision)
    }

    PantallaLista(onRefrescar = viewModel::cargar) {
        item {
            EncabezadoHome(
                saludo = "Hola, ${sesion?.primerNombre.orEmpty()}",
                titulo = "Directiva",
                rol = "DIRECTIVA"
            )
        }
        seccion(estado, viewModel::cargar) { datos ->
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Contador("Por revisar", datos.pendientes.size, Terracota, Modifier.weight(1f))
                    Contador("Reservas ${Fechas.mesCortoMayus(viewModel.mes).lowercase()}.", datos.reservasDelMes, Crema, Modifier.weight(1f))
                }
            }

            item { TituloSeccion("Solicitudes por revisar", accion = "Ver todas", onAccion = onVerSolicitudes) }
            if (datos.pendientes.isEmpty()) item { EstadoVacio("No hay solicitudes por revisar") }
            items(datos.pendientes.take(3), key = { "p${it.id}" }) { r ->
                TarjetaSolicitud(r, onAprobar = { viewModel.aprobar(r) }, onRechazar = { viewModel.rechazar(r) })
            }

            item { TituloSeccion("Próximas en la sede") }
            if (datos.proximas.isEmpty()) item { EstadoVacio("No hay reservas aprobadas próximas") }
            items(datos.proximas, key = { "a${it.id}" }) { r ->
                // "Sáb 22 · Cumpleaños"  /  "Ana Soto · Salón principal · 18:00 a 23:00"
                FilaReserva(
                    reserva = r,
                    titulo = "${Fechas.diaSemanaCorto(r.dia)} ${r.dia.dayOfMonth} · ${r.motivo.orEmpty()}",
                    subtitulo = "${r.usuarioNombre} · ${r.sedeNombre} · ${Fechas.horario(r.horaInicio, r.horaFin)}"
                )
            }
        }
    }
}

@Composable
private fun Contador(etiqueta: String, valor: Int, color: Color, modifier: Modifier) {
    Tarjeta(modifier) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
        Text("$valor", style = MaterialTheme.typography.displayMedium, color = color)
    }
}
