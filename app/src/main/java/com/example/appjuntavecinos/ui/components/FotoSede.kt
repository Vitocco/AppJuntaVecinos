package com.example.appjuntavecinos.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.data.remote.ApiConfig
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Terracota

/**
 * Foto de una sede.
 *  - [ruta]: ruta que manda el backend ("/api/sedes/fotos/salon-1.jpg"). Es la fuente PRINCIPAL:
 *    se carga con Coil desde el servidor.
 *  - [respaldo]: foto local de res/drawable. Se muestra mientras carga la del servidor
 *    y si no hay conexión. Por defecto se deduce del nombre del archivo.
 *  - Si no hay nada, se ve una casita terracota sobre fondo carbón.
 *
 * El modifier define el tamaño y la forma (clip) desde la pantalla que la usa.
 */
@Composable
fun FotoSede(
    ruta: String?,
    modifier: Modifier = Modifier,
    @DrawableRes respaldo: Int? = fotoLocalDe(ruta),
    descripcion: String? = null
) {
    Box(modifier = modifier.background(CarbonIcono), contentAlignment = Alignment.Center) {
        // Capa 1: placeholder de casita (se ve solo si no hay ninguna foto)
        Icon(Icons.Outlined.Home, contentDescription = null, tint = Terracota, modifier = Modifier.size(40.dp))

        // Capa 2: la foto (del servidor; mientras tanto/si falla, la local)
        val local = respaldo?.let { painterResource(it) }
        val url = ApiConfig.urlCompleta(ruta)
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = descripcion,
                placeholder = local,
                error = local,
                fallback = local,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (local != null) {
            androidx.compose.foundation.Image(
                painter = local,
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Busca la copia local de una foto del backend por el nombre del archivo:
 * "/api/sedes/fotos/salon-1.jpg" -> R.drawable.sede_salon_1
 * Se escribe a mano (sin reflexión) para que sea fácil de leer y de ampliar.
 */
@DrawableRes
fun fotoLocalDe(ruta: String?): Int? = when (ruta?.substringAfterLast('/')) {
    "salon-1.jpg" -> R.drawable.sede_salon_1
    "salon-2.jpg" -> R.drawable.sede_salon_2
    "cocina-1.jpg" -> R.drawable.sede_cocina_1
    "patio-1.jpg" -> R.drawable.sede_patio_1
    else -> null
}
