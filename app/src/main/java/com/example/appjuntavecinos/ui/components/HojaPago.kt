package com.example.appjuntavecinos.ui.components

import android.content.ClipData
import com.example.appjuntavecinos.util.AbrirWebpay
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.TerracotaSuave
import com.example.appjuntavecinos.util.Moneda
import com.example.appjuntavecinos.util.cuando
import kotlinx.coroutines.launch

/** Pasos de la hoja de pago: primero se elige el medio, luego se ve su detalle. */
private enum class PasoPago { OPCIONES, WEBPAY, TRANSFERENCIA, EFECTIVO }

// Datos FICTICIOS de la cuenta de la junta (solo para la demo)
private object CuentaJunta {
    const val TITULAR = "Junta de Vecinos Población Zaror"
    const val RUT = "65.123.456-7"
    const val BANCO = "Banco Estado"
    const val CUENTA = "Cuenta Vista N° 123456789"
    const val CORREO = "tesoreria@zaror.cl"
}

/**
 * Hoja "¿Cómo quieres pagar?" para una reserva aprobada:
 *  - Webpay: resumen + "Ir a pagar" (llama a pagos/webpay/iniciar vía [onIrAWebpay]).
 *  - Transferencia: datos de la cuenta, "Copiar datos" y "Entendido".
 *  - Efectivo en la sede: horario de Tesorería y "Entendido".
 * Transferencia y efectivo no llaman al backend: el pago lo registra el tesorero (pestaña Pagos).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaPago(
    reserva: ReservaDto,
    iniciandoWebpay: Boolean,
    onIrAWebpay: () -> Unit,
    onCerrar: () -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val alcance = rememberCoroutineScope()
    var paso by rememberSaveable(reserva.id) { mutableStateOf(PasoPago.OPCIONES) }
    // La hoja es una ventana aparte: su Snackbar va dentro para que se vea sobre el velo
    val mensajes = remember { SnackbarHostState() }

    // Cierra con la animación de la hoja y luego avisa
    val cerrar: () -> Unit = { alcance.launch { estadoHoja.hide() }.invokeOnCompletion { onCerrar() } }

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = estadoHoja, containerColor = CarbonCampo) {
        Box {
            AnimatedContent(targetState = paso, label = "pasoPago") { p ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    when (p) {
                        PasoPago.OPCIONES -> Opciones(onElegir = { paso = it })
                        PasoPago.WEBPAY -> DetalleWebpay(reserva, iniciandoWebpay, onIrAWebpay, onVolver = { paso = PasoPago.OPCIONES })
                        PasoPago.TRANSFERENCIA -> DetalleTransferencia(reserva, mensajes, onEntendido = cerrar)
                        PasoPago.EFECTIVO -> DetalleEfectivo(onEntendido = cerrar)
                    }
                }
            }
            SnackbarZaror(mensajes, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
        }
    }
}

// ---------------------------------------------------------------------
// Paso 1: elegir el medio de pago
// ---------------------------------------------------------------------
@Composable
private fun Opciones(onElegir: (PasoPago) -> Unit) {
    TextoConPunto("¿Cómo quieres pagar?", estilo = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(12.dp))
    FilaMedioPago(Icons.Outlined.CreditCard, "Webpay", "Tarjeta de débito o crédito") { onElegir(PasoPago.WEBPAY) }
    HorizontalDivider(color = CarbonIcono)
    FilaMedioPago(Icons.Outlined.AccountBalance, "Transferencia bancaria", "A la cuenta de la junta") { onElegir(PasoPago.TRANSFERENCIA) }
    HorizontalDivider(color = CarbonIcono)
    FilaMedioPago(Icons.Outlined.Payments, "Efectivo en la sede", "Directo a Tesorería") { onElegir(PasoPago.EFECTIVO) }
}

/** [ícono]  Título / descripción corta  > */
@Composable
private fun FilaMedioPago(icono: ImageVector, titulo: String, descripcion: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Box(
            Modifier.size(44.dp).clip(MaterialTheme.shapes.small).background(CarbonIcono),
            contentAlignment = Alignment.Center
        ) { Icon(icono, contentDescription = null, tint = TerracotaSuave, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = Crema)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = GrisCalido)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = GrisCalido)
    }
}

// ---------------------------------------------------------------------
// Webpay: resumen de la reserva
// ---------------------------------------------------------------------
@Composable
private fun DetalleWebpay(reserva: ReservaDto, cargando: Boolean, onIrAPagar: () -> Unit, onVolver: () -> Unit) {
    TextoConPunto("Pagar con Webpay", estilo = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        FotoSede(
            ruta = reserva.sedeFoto,
            descripcion = reserva.sedeNombre,
            modifier = Modifier.size(72.dp).clip(MaterialTheme.shapes.medium)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(reserva.sedeNombre.orEmpty(), style = MaterialTheme.typography.titleMedium, color = Crema)
            Text(reserva.cuando, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
            if (!reserva.motivo.isNullOrBlank()) {
                Text(reserva.motivo, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
            }
        }
    }
    Monto(reserva.precio)
    Text(
        "Serás redirigido a Webpay, ambiente de prueba, sin cobros reales.",
        style = MaterialTheme.typography.bodySmall,
        color = GrisCalido
    )
    Spacer(Modifier.height(20.dp))
    BotonAccion("Ir a pagar", onClick = onIrAPagar, cargando = cargando)
    Spacer(Modifier.height(10.dp))
    BotonPrincipal("Volver", onClick = onVolver, habilitado = !cargando)
}

// ---------------------------------------------------------------------
// Transferencia: datos de la cuenta (ficticios)
// ---------------------------------------------------------------------
@Composable
private fun DetalleTransferencia(reserva: ReservaDto, mensajes: SnackbarHostState, onEntendido: () -> Unit) {
    val context = LocalContext.current
    val alcance = rememberCoroutineScope()
    val datos = listOf(
        "Titular" to CuentaJunta.TITULAR,
        "RUT" to CuentaJunta.RUT,
        "Banco" to CuentaJunta.BANCO,
        "Cuenta" to CuentaJunta.CUENTA,
        "Correo" to CuentaJunta.CORREO
    )

    TextoConPunto("Transferencia bancaria", estilo = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(14.dp))
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(CarbonIcono).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        datos.forEach { (etiqueta, valor) ->
            Row {
                Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = GrisCalido, modifier = Modifier.width(72.dp))
                Text(valor, style = MaterialTheme.typography.bodyMedium, color = Crema)
            }
        }
    }
    Monto(reserva.precio)
    Text(
        "Indica tu nombre y la fecha de la reserva en el comentario. La Tesorería registrará tu pago y la reserva aparecerá como Pagada.",
        style = MaterialTheme.typography.bodyMedium,
        color = GrisCalido
    )
    Spacer(Modifier.height(20.dp))
    BotonAccion(
        "Copiar datos",
        icono = Icons.Outlined.ContentCopy,
        onClick = {
            val texto = (datos.map { it.second } + "Monto: ${Moneda.pesos(reserva.precio)}").joinToString("\n")
            copiarAlPortapapeles(context, texto)
            // Largo (10 s): en Android 13+ la vista previa del portapapeles del sistema tapa
            // la parte de abajo unos segundos; así el aviso sigue visible cuando se va.
            alcance.launch { mensajes.showSnackbar("Datos copiados", duration = SnackbarDuration.Long) }
        }
    )
    Spacer(Modifier.height(10.dp))
    BotonPrincipal("Entendido", onClick = onEntendido)
}

private fun copiarAlPortapapeles(context: Context, texto: String) {
    val portapapeles = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    portapapeles.setPrimaryClip(ClipData.newPlainText("Datos de transferencia", texto))
}

// ---------------------------------------------------------------------
// Efectivo en la sede
// ---------------------------------------------------------------------
@Composable
private fun DetalleEfectivo(onEntendido: () -> Unit) {
    TextoConPunto("Efectivo en la sede", estilo = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(44.dp).clip(MaterialTheme.shapes.small).background(CarbonIcono),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Outlined.Payments, contentDescription = null, tint = TerracotaSuave, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(14.dp))
        Text(
            "Paga directamente a Tesorería en la sede (Los Jazmines 425), martes y jueves de 18:00 a 20:00. " +
                "Recibirás una boleta y tu reserva quedará como Pagada.",
            style = MaterialTheme.typography.bodyMedium,
            color = Crema,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(20.dp))
    BotonPrincipal("Entendido", onClick = onEntendido)
}

/** "Monto" + valor en grande. */
@Composable
private fun Monto(precio: Long) {
    Spacer(Modifier.height(18.dp))
    Text("Monto", style = MaterialTheme.typography.labelMedium, color = GrisCalido)
    Text(
        Moneda.pesos(precio),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.SemiBold,
        color = Crema
    )
    Spacer(Modifier.height(8.dp))
}

/**
 * Cuando el ViewModel entrega la URL de Webpay (evento de un uso), la marca como
 * consumida y abre la pantalla de pago (PagoWebpayScreen).
 */
@Composable
fun AlRecibirUrlWebpay(url: AbrirWebpay?, onConsumida: () -> Unit, onAbrir: (AbrirWebpay) -> Unit) {
    LaunchedEffect(url) {
        if (url != null) {
            onConsumida()
            onAbrir(url)
        }
    }
}
