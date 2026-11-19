package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.data.remote.dto.GastoCategoriaDto
import com.example.appjuntavecinos.ui.theme.Ambar
import com.example.appjuntavecinos.ui.theme.AmbarFondo
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.theme.VerdeOk
import com.example.appjuntavecinos.ui.theme.VerdeOkFondo
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Moneda
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

// =====================================================================
// "¿En qué se gastó este mes?": una barra por categoría (la mayor = 100 %)
// =====================================================================
@Composable
fun BarrasGastos(gastos: List<GastoCategoriaDto>, modifier: Modifier = Modifier) {
    Tarjeta(modifier) {
        if (gastos.isEmpty()) {
            Text("Sin gastos registrados en este período", style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
            return@Tarjeta
        }
        val maximo = gastos.maxOf { it.total }.coerceAtLeast(1)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            gastos.sortedByDescending { it.total }.forEach { g ->
                // la barra crece animada desde 0 al abrir la pantalla
                val proporcion by animateFloatAsState(g.total.toFloat() / maximo, tween(700), label = "barra")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(g.categoria, style = MaterialTheme.typography.bodyLarge, color = Crema)
                        Text(Moneda.pesos(g.total), style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
                    }
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(CarbonIcono)) {
                        Box(Modifier.fillMaxWidth(proporcion).fillMaxHeight().clip(CircleShape).background(Terracota))
                    }
                }
            }
        }
    }
}

// =====================================================================
// Chips de mes: "Jun  Jul  ...  [Nov 2026]". El elegido muestra también el año.
// =====================================================================
@Composable
fun ChipsMes(
    meses: List<YearMonth>,
    seleccionado: YearMonth,
    onSeleccionar: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val estadoLista = rememberLazyListState()
    // al abrir, se ve el mes elegido (normalmente el último = actual)
    LaunchedEffect(Unit) { estadoLista.scrollToItem(meses.indexOf(seleccionado).coerceAtLeast(0)) }
    LazyRow(
        state = estadoLista,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(meses) { mes ->
            val elegido = mes == seleccionado
            ChipFiltro(
                texto = if (elegido) "${Fechas.mesCortoMayus(mes)} ${mes.year}" else Fechas.mesCortoMayus(mes),
                seleccionado = elegido,
                onClick = { onSeleccionar(mes) }
            )
        }
    }
}

// =====================================================================
// Calendario mensual en grilla (detalle de sede, diseño 5)
// =====================================================================

/** Cómo se pinta cada día: con reserva aprobada, pendiente o libre. */
enum class TipoDia { LIBRE, APROBADA, PENDIENTE }

@Composable
fun CalendarioMes(
    mes: YearMonth,
    tipoDe: (LocalDate) -> TipoDia,
    diaElegido: LocalDate?,
    onDia: (LocalDate) -> Unit,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Encabezado: "Noviembre 2026   <  >"
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(Fechas.mesAnio(mes), style = MaterialTheme.typography.titleLarge, color = Crema, modifier = Modifier.weight(1f))
            IconButton(onClick = onMesAnterior) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "Mes anterior", tint = Terracota)
            }
            IconButton(onClick = onMesSiguiente) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Mes siguiente", tint = Terracota)
            }
        }
        // Días de la semana (la semana parte el lunes)
        Row(Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                Text(it, style = MaterialTheme.typography.labelMedium, color = GrisCalido,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        // Casillas vacías antes del día 1 + los días del mes, en filas de 7
        val vaciosAntes = mes.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
        val casillas: List<LocalDate?> = List(vaciosAntes) { null } + (1..mes.lengthOfMonth()).map { mes.atDay(it) }
        casillas.chunked(7).forEach { semana ->
            Row(Modifier.fillMaxWidth()) {
                semana.forEach { dia ->
                    Box(Modifier.weight(1f).aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
                        if (dia != null) CasillaDia(dia, tipoDe(dia), dia == diaElegido) { onDia(dia) }
                    }
                }
                repeat(7 - semana.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        // Leyenda
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 4.dp)) {
            Leyenda(VerdeOkFondo, "Aprobada")
            Leyenda(AmbarFondo, "Pendiente")
            Leyenda(Crema, "Elegido")
        }
    }
}

@Composable
private fun CasillaDia(dia: LocalDate, tipo: TipoDia, elegido: Boolean, onClick: () -> Unit) {
    val (fondo, texto) = when {
        elegido -> Crema to Carbon
        tipo == TipoDia.APROBADA -> VerdeOkFondo to VerdeOk
        tipo == TipoDia.PENDIENTE -> AmbarFondo to Ambar
        else -> Color.Transparent to Crema
    }
    Box(
        Modifier.fillMaxWidth().fillMaxHeight().clip(RoundedCornerShape(14.dp)).background(fondo).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${dia.dayOfMonth}",
            style = MaterialTheme.typography.titleSmall,
            color = if (dia.isBefore(Fechas.hoy()) && !elegido) texto.copy(alpha = 0.45f) else texto
        )
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = GrisCalido, modifier = Modifier.padding(start = 6.dp))
    }
}
