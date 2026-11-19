package com.example.appjuntavecinos.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.util.CamaraUriFactory

/**
 * Comprobante: dos cajas punteadas "Cámara" y "Galería" (diseño 4). Al elegir una foto
 * se muestra la vista previa con una X para quitarla.
 * Reutiliza el flujo que ya tenía la app: permiso de cámara -> TakePicture con una Uri
 * de CamaraUriFactory; galería con GetContent + copia a la carpeta de la app.
 * [abrirCamaraAlInicio] = true abre la cámara apenas se muestra (acceso "Boleta").
 */
@Composable
fun SelectorComprobante(
    foto: Uri?,
    onFoto: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
    obligatorio: Boolean = false,
    error: String? = null,
    abrirCamaraAlInicio: Boolean = false
) {
    val context = LocalContext.current
    var uriPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }

    // ---------- Cámara ----------
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) onFoto(uriPendiente)   // solo se guarda si la foto se tomó bien
        uriPendiente = null
    }

    fun abrirCamara() {
        try {
            val uri = CamaraUriFactory.crearUri(context)
            uriPendiente = uri
            camara.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(context, "Debes conceder el permiso de cámara en Ajustes.", Toast.LENGTH_LONG).show()
    }

    fun tomarFoto() {
        val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (concedido) abrirCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
    }

    // ---------- Galería ----------
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { elegida ->
        if (elegida != null) {
            try {
                onFoto(CamaraUriFactory.copiarDesdeGaleria(context, elegida))
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo cargar la imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Acceso directo "Boleta": abre la cámara una sola vez al entrar
    var yaAbrio by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(abrirCamaraAlInicio) {
        if (abrirCamaraAlInicio && !yaAbrio) { yaAbrio = true; tomarFoto() }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            if (obligatorio) "Comprobante (obligatorio)" else "Comprobante (opcional)",
            style = MaterialTheme.typography.labelMedium,
            color = GrisCalido,
            modifier = Modifier.padding(start = 2.dp)
        )
        if (foto != null) {
            // Vista previa con botón para quitarla
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(CarbonIcono)
            ) {
                AsyncImage(
                    model = foto,
                    contentDescription = "Foto del comprobante",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(34.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(Carbon.copy(alpha = 0.75f))
                        .clickable { onFoto(null) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Close, contentDescription = "Quitar foto", tint = Crema) }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CajaPunteada("Cámara", Icons.Outlined.PhotoCamera, error != null, ::tomarFoto, Modifier.weight(1f))
                CajaPunteada("Galería", Icons.Outlined.Image, error != null, { galeria.launch("image/*") }, Modifier.weight(1f))
            }
        }
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = RojoError, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun CajaPunteada(texto: String, icono: ImageVector, conError: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val color = if (conError) RojoError else CarbonIcono
    Column(
        modifier = modifier
            .height(104.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .bordePunteado(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, contentDescription = null, tint = Terracota, modifier = Modifier.size(28.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = Crema, modifier = Modifier.padding(top = 8.dp))
    }
}

// Borde punteado redondeado (Compose no trae uno listo): se dibuja sobre el contenido
private fun Modifier.bordePunteado(color: Color) = drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
        cornerRadius = CornerRadius(20.dp.toPx())
    )
}
