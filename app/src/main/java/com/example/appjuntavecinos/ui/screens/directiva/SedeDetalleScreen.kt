package com.example.appjuntavecinos.ui.screens.directiva

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AvisoSinConexion
import com.example.appjuntavecinos.ui.components.BotonVolver
import com.example.appjuntavecinos.ui.components.CalendarioMes
import com.example.appjuntavecinos.ui.components.EstadoCarga
import com.example.appjuntavecinos.ui.components.EstadoError
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FilaReserva
import com.example.appjuntavecinos.ui.components.FondoZaror
import com.example.appjuntavecinos.ui.components.FotoSede
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.TipoDia
import com.example.appjuntavecinos.ui.components.TituloSeccion
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.viewmodel.EstadoUi
import com.example.appjuntavecinos.ui.viewmodel.SedeDetalleViewModel
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Moneda

/**
 * Detalle de la sede (diseño 5): foto, capacidad y precio, calendario mensual
 * (verde = aprobada, ámbar = pendiente) y las reservas del día elegido.
 */
@Composable
fun SedeDetalleScreen(
    onVolver: () -> Unit,
    viewModel: SedeDetalleViewModel = viewModel()
) {
    val sede by viewModel.sede.collectAsState()
    val mes by viewModel.mes.collectAsState()
    val dia by viewModel.dia.collectAsState()
    val ocupaciones by viewModel.ocupaciones.collectAsState()
    val reservas by viewModel.reservas.collectAsState()

    FondoZaror {
        when (val s = sede) {
            EstadoUi.Cargando -> Box(Modifier.statusBarsPadding()) { EstadoCarga() }
            is EstadoUi.Error -> Column(Modifier.statusBarsPadding().padding(24.dp)) {
                BotonVolver(onVolver)
                EstadoError(s.mensaje, onReintentar = viewModel::cargar)
            }
            is EstadoUi.Listo -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()
            ) {
                val info = s.datos
                // ---------------- Foto ----------------
                Box(Modifier.fillMaxWidth().height(240.dp)) {
                    FotoSede(ruta = info.fotos?.firstOrNull(), descripcion = info.nombre, modifier = Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Carbon)))
                    BotonVolver(onVolver, Modifier.statusBarsPadding().padding(start = 20.dp, top = 12.dp))
                }

                Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (s.desdeCache) AvisoSinConexion()
                    Column {
                        TextoConPunto(info.nombre, estilo = MaterialTheme.typography.displaySmall)
                        Text(
                            "Capacidad ${info.capacidad} personas · ${Moneda.pesos(info.precio)} por evento",
                            style = MaterialTheme.typography.bodyLarge, color = GrisCalido
                        )
                    }

                    // ---------------- Calendario ----------------
                    CalendarioMes(
                        mes = mes,
                        tipoDe = { d ->
                            val delDia = ocupaciones.filter { it.fecha == Fechas.aApi(d) }
                            when {
                                delDia.any { it.estado == "APROBADA" } -> TipoDia.APROBADA
                                delDia.isNotEmpty() -> TipoDia.PENDIENTE
                                else -> TipoDia.LIBRE
                            }
                        },
                        diaElegido = dia,
                        onDia = viewModel::elegirDia,
                        onMesAnterior = { viewModel.cambiarMes(-1) },
                        onMesSiguiente = { viewModel.cambiarMes(1) }
                    )

                    // ---------------- Reservas del día elegido ----------------
                    TituloSeccion(Fechas.diaCompleto(dia))
                    when (val r = reservas) {
                        EstadoUi.Cargando -> EstadoCarga()
                        is EstadoUi.Error -> EstadoError(r.mensaje, onReintentar = viewModel::cargar)
                        is EstadoUi.Listo -> {
                            val delDia = r.datos.filter { it.fecha == Fechas.aApi(dia) }
                            if (delDia.isEmpty()) EstadoVacio("Sin reservas este día")
                            delDia.forEach { res ->
                                // "18:00 a 23:00"  /  "Camila Rojas · Cumpleaños"
                                FilaReserva(
                                    reserva = res,
                                    titulo = Fechas.horario(res.horaInicio, res.horaFin),
                                    subtitulo = "${res.usuarioNombre} · ${res.motivo.orEmpty()}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
