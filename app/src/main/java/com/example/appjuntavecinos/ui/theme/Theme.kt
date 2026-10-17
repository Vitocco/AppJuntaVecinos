package com.example.appjuntavecinos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeJunta,              // color de Button, íconos activos, borde del campo enfocado
    onPrimary = Color.White,           // texto/ícono ENCIMA de primary (ej. "Ingresar" dentro del botón)
    primaryContainer = VerdeClaro,     // fondos suaves de elementos seleccionados
    onPrimaryContainer = VerdeJunta,   // texto encima de VerdeClaro
    secondaryContainer = VerdeClaro,   // el SegmentedButton seleccionado usa ESTE color
    onSecondaryContainer = VerdeJunta,
    background = FondoApp,             // ← este es el que usa tu Surface
    surface = FondoApp,                // fondo de Cards, barras, etc.
    onBackground = TextoPrincipal,     // texto sobre el fondo
    onSurface = TextoPrincipal,
    onSurfaceVariant = TextoSecundario,// el gris de los subtítulos
    error = RojoError
)

@Composable
fun AppJuntaVecinosTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}