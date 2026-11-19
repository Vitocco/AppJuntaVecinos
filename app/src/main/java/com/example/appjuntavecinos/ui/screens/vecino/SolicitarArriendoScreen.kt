package com.example.appjuntavecinos.ui.screens.vecino

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.data.remote.dto.SedeDto
import com.example.appjuntavecinos.ui.components.BotonAccion
import com.example.appjuntavecinos.ui.components.BotonVolver
import com.example.appjuntavecinos.ui.components.CampoTexto
import com.example.appjuntavecinos.ui.components.EstadoCarga
import com.example.appjuntavecinos.ui.components.EstadoError
import com.example.appjuntavecinos.ui.components.FondoZaror
import com.example.appjuntavecinos.ui.components.FotoSede
import com.example.appjuntavecinos.ui.components.MensajeError
import com.example.appjuntavecinos.ui.components.SelectorHora
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.theme.Ambar
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.theme.TerracotaSuave
import com.example.appjuntavecinos.ui.viewmodel.EstadoUi
import com.example.appjuntavecinos.ui.viewmodel.SolicitarArriendoViewModel
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Moneda
import java.time.LocalDate

/**
 * Solicitar arriendo (diseño 6): foto grande de la sede (deslizable si tiene varias),
 * miniaturas para elegir sede, fila de días con los ocupados, Desde/Hasta, Motivo/Personas
 * y "Enviar solicitud · $precio" (POST reservas).
 */
@Composable
fun SolicitarArriendoScreen(
    onVolver: () -> Unit,
    onEnviada: () -> Unit,
    viewModel: SolicitarArriendoViewModel = viewModel()
) {
    val sedes by viewModel.sedes.collectAsState()
    val sede by viewModel.sede.collectAsState()
    val ocupaciones by viewModel.ocupaciones.collectAsState()
    val form by viewModel.form.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val enviando by viewModel.enviando.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val enviada by viewModel.enviada.collectAsState()

    // El ViewModel avisa "enviada"; la pantalla avisa a la navegación (no navega ella)
    LaunchedEffect(enviada) { if (enviada) onEnviada() }

    FondoZaror {
        when (val s = sedes) {
            EstadoUi.Cargando -> Box(Modifier.statusBarsPadding()) { EstadoCarga() }
            is EstadoUi.Error -> Column(Modifier.statusBarsPadding().padding(24.dp)) {
                BotonVolver(onVolver)
                EstadoError(s.mensaje, onReintentar = viewModel::cargarSedes)
            }
            is EstadoUi.Listo -> Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // ---------------- Foto grande + título ----------------
                sede?.let { CabeceraSede(it, onVolver) }

                Column(
                    Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ---------------- Elige la sede ----------------
                    Etiqueta("Elige la sede")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.datos.filter { it.activa }, key = { it.id }) { opcion ->
                            MiniaturaSede(opcion, elegida = opcion.id == sede?.id) { viewModel.elegirSede(opcion) }
                        }
                    }

                    // ---------------- Días ----------------
                    Etiqueta("Día · ${Fechas.nombreMes(java.time.YearMonth.from(form.fecha ?: viewModel.dias.first()))}")
                    when (val o = ocupaciones) {
                        is EstadoUi.Error -> EstadoError(o.mensaje, onReintentar = viewModel::cargarDisponibilidad)
                        else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(viewModel.dias) { dia ->
                                CasillaDiaSemana(
                                    dia = dia,
                                    elegido = dia == form.fecha,
                                    ocupado = viewModel.ocupacionesDe(dia).isNotEmpty(),
                                    onClick = { viewModel.elegirDia(dia) }
                                )
                            }
                        }
                    }
                    errores.fecha?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = RojoError) }
                    // Horarios ya tomados el día elegido
                    form.fecha?.let { dia ->
                        val tomados = viewModel.ocupacionesDe(dia)
                        if (tomados.isNotEmpty()) {
                            Text(
                                "Ocupado ese día: " + tomados.joinToString { Fechas.horario(it.horaInicio, it.horaFin) },
                                style = MaterialTheme.typography.bodySmall,
                                color = Ambar
                            )
                        }
                    }

                    // ---------------- Desde / Hasta ----------------
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        SelectorHora("Desde", form.desde, Fechas.horasInicio, viewModel::elegirDesde,
                            Modifier.weight(1f), errores.desde)
                        SelectorHora("Hasta", form.hasta, Fechas.horasFin, viewModel::elegirHasta,
                            Modifier.weight(1f), errores.hasta)
                    }

                    // ---------------- Motivo / Personas ----------------
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        CampoTexto(
                            etiqueta = "Motivo",
                            valor = form.motivo,
                            onValorChange = viewModel::actualizarMotivo,
                            placeholder = "Ej: cumpleaños",
                            error = errores.motivo,
                            modifier = Modifier.weight(1.6f)
                        )
                        CampoTexto(
                            etiqueta = "Personas",
                            valor = form.personas,
                            onValorChange = viewModel::actualizarPersonas,
                            placeholder = "máx. ${sede?.capacidad ?: ""}",
                            tipoTeclado = KeyboardType.Number,
                            error = errores.personas,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MensajeError(mensajeError)
                    BotonAccion(
                        texto = "Enviar solicitud",
                        textoDerecha = sede?.let { Moneda.pesos(it.precio) },
                        onClick = viewModel::enviar,
                        cargando = enviando
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

/** Foto grande (HorizontalPager con puntos si hay más de una) + "Solicitar arriendo / <Sede>." */
@Composable
private fun CabeceraSede(sede: SedeDto, onVolver: () -> Unit) {
    val fotos = sede.fotos.orEmpty().ifEmpty { listOf<String?>(null) }
    // key: al cambiar de sede el pager vuelve a la primera foto
    key(sede.id) {
        val pager = rememberPagerState { fotos.size }
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { pagina ->
                FotoSede(ruta = fotos[pagina], descripcion = sede.nombre, modifier = Modifier.fillMaxSize())
            }
            // degradado para que el título se lea sobre la foto
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0.45f to Color.Transparent, 1f to Carbon)
                )
            )
            BotonVolver(onVolver, Modifier.statusBarsPadding().padding(start = 20.dp, top = 12.dp))
            Row(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Solicitar arriendo", style = MaterialTheme.typography.bodyLarge, color = TerracotaSuave)
                    TextoConPunto(sede.nombre, estilo = MaterialTheme.typography.displaySmall)
                }
                // Puntos solo si la sede tiene más de una foto
                if (fotos.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                        repeat(fotos.size) { i ->
                            val activa = i == pager.currentPage
                            Box(
                                Modifier.height(6.dp).width(if (activa) 22.dp else 6.dp)
                                    .clip(CircleShape).background(if (activa) Crema else GrisCalido)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniaturaSede(sede: SedeDto, elegida: Boolean, onClick: () -> Unit) {
    Column(Modifier.width(140.dp).clickable(onClick = onClick)) {
        FotoSede(
            ruta = sede.fotos?.firstOrNull(),
            descripcion = sede.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(2.dp, if (elegida) Terracota else Color.Transparent, RoundedCornerShape(18.dp))
        )
        // "Salón · 80"
        Text(
            "${sede.nombre.substringBefore(' ')} · ${sede.capacidad}",
            style = MaterialTheme.typography.labelMedium,
            color = if (elegida) Crema else GrisCalido,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** "Lun / 17". Elegido = crema; con reservas = número ámbar y un punto. */
@Composable
private fun CasillaDiaSemana(dia: LocalDate, elegido: Boolean, ocupado: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (elegido) Crema else CarbonCampo)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(Fechas.diaSemanaCorto(dia), style = MaterialTheme.typography.labelMedium,
            color = if (elegido) Carbon else GrisCalido)
        Text("${dia.dayOfMonth}", style = MaterialTheme.typography.headlineSmall,
            color = when { elegido -> Carbon; ocupado -> Ambar; else -> Crema })
        Box(Modifier.padding(top = 2.dp).size(5.dp).clip(CircleShape)
            .background(if (ocupado) Ambar else Color.Transparent))
    }
}

@Composable
private fun Etiqueta(texto: String) =
    Text(texto, style = MaterialTheme.typography.labelMedium, color = GrisCalido)
