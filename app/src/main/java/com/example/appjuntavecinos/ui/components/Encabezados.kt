package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.AmbarFondo
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.DegradadoSaldo
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.theme.TerracotaSuave
import com.example.appjuntavecinos.util.Moneda

/** Texto con el punto final en terracota: "Junta Zaror." / "Crear cuenta." / "Salón principal." */
@Composable
fun TextoConPunto(texto: String, estilo: TextStyle, modifier: Modifier = Modifier, color: Color = Crema) {
    Text(
        text = buildAnnotatedString {
            append(texto)
            withStyle(SpanStyle(color = Terracota)) { append(".") }
        },
        style = estilo,
        color = color,
        modifier = modifier
    )
}

/** Logo: cuadrito terracota con una casa + "Junta Zaror". */
@Composable
fun LogoZaror(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Terracota),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Home, contentDescription = null, tint = Crema, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text("Junta Zaror", style = MaterialTheme.typography.titleLarge, color = TerracotaSuave)
    }
}

/**
 * Encabezado de los Inicio:  "Hola, <saludo>"  /  <título grande>   [ROL]  [campana]
 * - Vecino:    saludo = "Hola,"           título = nombre completo
 * - Directiva: saludo = "Hola, <nombre>"  título = "Directiva"  rol = "DIRECTIVA"
 */
@Composable
fun EncabezadoHome(
    saludo: String,
    titulo: String,
    modifier: Modifier = Modifier,
    rol: String? = null,
    hayAvisos: Boolean = false,
    onCampana: () -> Unit = {}
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(saludo, style = MaterialTheme.typography.bodyLarge, color = GrisCalido)
            Text(titulo, style = MaterialTheme.typography.headlineLarge, color = Crema, maxLines = 1)
        }
        if (rol != null) {
            BadgeRol(rol)
            Spacer(Modifier.width(10.dp))
        }
        // Campana con puntito terracota si hay avisos
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CarbonCampo)
                .clickable(onClick = onCampana),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Avisos", tint = Crema)
            if (hayAvisos) {
                Box(
                    Modifier
                        .offset(x = 7.dp, y = (-8).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Terracota)
                )
            }
        }
    }
}

/** Etiqueta del rol: "DIRECTIVA", "TESORERO"... (terracota sobre fondo ámbar). */
@Composable
fun BadgeRol(rol: String, modifier: Modifier = Modifier) {
    Text(
        rol,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = TerracotaSuave,
        modifier = modifier
            .clip(CircleShape)
            .background(AmbarFondo)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/**
 * Tarjeta naranja con el saldo de la junta (degradado 135°, texto blanco).
 *   Saldo de la junta · Noviembre
 *   $ 412.300
 *   Ingresos + $105.000     Gastos − $48.700
 */
@Composable
fun TarjetaSaldo(
    etiqueta: String,
    saldo: Long,
    ingresos: Long,
    gastos: Long,
    modifier: Modifier = Modifier
) {
    val blancoSuave = Color.White.copy(alpha = 0.82f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(DegradadoSaldo)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = blancoSuave)
        Text(Moneda.pesos(saldo), style = MaterialTheme.typography.displayMedium, color = Color.White)
        Spacer(Modifier.size(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column {
                Text("Ingresos", style = MaterialTheme.typography.bodySmall, color = blancoSuave)
                Text(Moneda.ingreso(ingresos), style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Column {
                Text("Gastos", style = MaterialTheme.typography.bodySmall, color = blancoSuave)
                Text(Moneda.gasto(gastos), style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

/** Título de pantalla con botón volver opcional (Solicitar arriendo, Registrar pago...). */
@Composable
fun TituloPantalla(
    titulo: String,
    modifier: Modifier = Modifier,
    onVolver: (() -> Unit)? = null
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onVolver != null) {
            BotonVolver(onClick = onVolver)
            Spacer(Modifier.width(14.dp))
        }
        Text(titulo, style = MaterialTheme.typography.headlineMedium, color = Crema)
    }
}
