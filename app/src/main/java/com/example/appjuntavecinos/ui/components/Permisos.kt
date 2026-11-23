package com.example.appjuntavecinos.ui.components

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Nombre del permiso como texto: así compila aunque el SDK no traiga la constante.
private const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"

// Android 17 = API 37
private const val API_ANDROID_17 = 37

/**
 * Android 17 bloquea las conexiones a IPs privadas (como 10.0.2.2, el PC visto
 * desde el emulador) si la app no tiene el permiso de "red local".
 * Este composable lo pide UNA vez al abrir la app. En Android 16 o menor no hace nada.
 * Si el usuario lo rechaza, las pantallas mostrarán "No se pudo conectar con el servidor".
 */
@Composable
fun PedirPermisoRedLocal() {
    if (Build.VERSION.SDK_INT < API_ANDROID_17) return

    val context = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        val concedido = ContextCompat.checkSelfPermission(context, PERMISO_RED_LOCAL) ==
            PackageManager.PERMISSION_GRANTED
        if (!concedido) lanzador.launch(PERMISO_RED_LOCAL)
    }
}

/**
 * Android 13+ pide permiso para mostrar notificaciones. Lo usa el aviso
 * "Solicitud recibida" (AvisoWorker) al enviar una reserva. Se pide una vez.
 */
@Composable
fun PedirPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val permiso = android.Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            lanzador.launch(permiso)
        }
    }
}
