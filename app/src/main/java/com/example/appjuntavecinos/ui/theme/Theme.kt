package com.example.appjuntavecinos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

// Tema oscuro ÚNICO: la app se ve igual aunque el celular esté en modo claro.
// Mapeamos nuestra paleta a los "roles" de Material 3 para que los componentes
// estándar (DatePicker, diálogos, Snackbar) también queden con los colores de la junta.
private val EsquemaZaror = darkColorScheme(
    primary = Terracota,
    onPrimary = Crema,
    primaryContainer = AmbarFondo,
    onPrimaryContainer = TerracotaSuave,
    secondary = Crema,
    onSecondary = Carbon,
    secondaryContainer = CarbonCampoOn,
    onSecondaryContainer = Crema,
    tertiary = VerdeOk,
    onTertiary = Carbon,
    background = Carbon,
    onBackground = Crema,
    surface = Carbon,
    onSurface = Crema,
    surfaceVariant = CarbonCampo,
    onSurfaceVariant = GrisCalido,
    surfaceContainerLowest = Carbon,
    surfaceContainerLow = CarbonCampo,
    surfaceContainer = CarbonCampo,
    surfaceContainerHigh = CarbonCampoOn,
    surfaceContainerHighest = CarbonIcono,
    inverseSurface = Crema,
    inverseOnSurface = Carbon,
    outline = CarbonIcono,
    outlineVariant = CarbonCampoOn,
    error = RojoError,
    onError = Crema,
    errorContainer = RojoErrorFondo,
    onErrorContainer = RojoError
)

// Formas del diseño: filas 18dp, campos 20dp, tarjetas 24dp
val FormasZaror = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(18.dp),   // filas
    medium = RoundedCornerShape(20.dp),  // campos de texto
    large = RoundedCornerShape(24.dp),   // tarjetas
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AppJuntaVecinosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaZaror,
        typography = Typography,
        shapes = FormasZaror
    ) {
        // Color por defecto de TODO texto e ícono que no indique uno (fuera de un Surface
        // sería negro). Así ningún texto queda invisible sobre el fondo carbón.
        CompositionLocalProvider(LocalContentColor provides Crema, content = content)
    }
}
