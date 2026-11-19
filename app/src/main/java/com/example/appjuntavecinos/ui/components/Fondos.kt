package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.appjuntavecinos.ui.theme.BrilloTerracota
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.Terracota

/**
 * Fondo de todas las pantallas: carbón + un brillo terracota difuso arriba a la derecha
 * (radialGradient con alfa 0.28 que se desvanece a transparente).
 */
@Composable
fun FondoZaror(
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Carbon)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(BrilloTerracota, Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 1.1f
                    )
                )
            },
        content = contenido
    )
}

/**
 * Silueta de casas de la población al pie del login (dibujada con Canvas, sin imágenes).
 * Dos capas: casas atrás (con techo a dos aguas) y lomas adelante, más unas
 * ventanitas terracota "encendidas".
 */
@Composable
fun CasasFondo(modifier: Modifier = Modifier) {
    val colorAtras = Color(0xFF221F1C)
    val colorAdelante = CarbonCampo

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // ---- Capa de atrás: casas con techo (x inicial, ancho, alto pared, alto techo) en % ----
        val casas = listOf(
            listOf(0.00f, 0.22f, 0.30f, 0.22f),
            listOf(0.14f, 0.18f, 0.42f, 0.22f),
            listOf(0.30f, 0.20f, 0.30f, 0.18f),
            listOf(0.44f, 0.20f, 0.48f, 0.22f),
            listOf(0.62f, 0.16f, 0.32f, 0.16f),
            listOf(0.74f, 0.18f, 0.36f, 0.18f),
            listOf(0.88f, 0.16f, 0.44f, 0.22f)
        )
        casas.forEach { (x, ancho, pared, techo) ->
            val izq = x * w
            val der = (x + ancho) * w
            val basePared = h - pared * h
            val punta = basePared - techo * h
            val casa = Path().apply {
                moveTo(izq, h)
                lineTo(izq, basePared)
                lineTo((izq + der) / 2f, punta)   // techo a dos aguas
                lineTo(der, basePared)
                lineTo(der, h)
                close()
            }
            drawPath(casa, colorAtras)
        }

        // ---- Capa de adelante: lomas suaves ----
        val lomas = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.72f)
            lineTo(w * 0.12f, h * 0.62f)
            lineTo(w * 0.28f, h * 0.70f)
            lineTo(w * 0.42f, h * 0.58f)
            lineTo(w * 0.56f, h * 0.66f)
            lineTo(w * 0.72f, h * 0.56f)
            lineTo(w * 0.86f, h * 0.68f)
            lineTo(w, h * 0.60f)
            lineTo(w, h)
            close()
        }
        drawPath(lomas, colorAdelante)

        // ---- Ventanas encendidas (cuadraditos terracota) ----
        val lado = w * 0.018f
        listOf(
            0.06f to 0.66f, 0.21f to 0.52f, 0.36f to 0.66f,
            0.51f to 0.48f, 0.65f to 0.70f, 0.81f to 0.58f, 0.93f to 0.50f
        ).forEach { (px, py) ->
            drawRect(Terracota, topLeft = Offset(px * w, py * h), size = Size(lado, lado))
        }
    }
}
