package com.example.appjuntavecinos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R

// Fuente Outfit (res/font). Se carga cada grosor por separado
// para que funcione desde Android 7 (minSdk 24).
val Outfit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold)
)

// Estilo base: todos los textos usan Outfit
private val base = TextStyle(fontFamily = Outfit)

val Typography = Typography(
    // Título grande del login y de "Crear cuenta." (40sp SemiBold, letras apretadas)
    displayLarge = base.copy(fontSize = 40.sp, fontWeight = FontWeight.SemiBold, lineHeight = 44.sp, letterSpacing = (-0.8).sp),
    displayMedium = base.copy(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, lineHeight = 40.sp, letterSpacing = (-0.6).sp),
    displaySmall = base.copy(fontSize = 30.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    // Títulos de pantalla ("Mis reservas", "Camila Rojas")
    headlineLarge = base.copy(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 34.sp, letterSpacing = (-0.4).sp),
    headlineMedium = base.copy(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    headlineSmall = base.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
    // Títulos de sección y de tarjeta
    titleLarge = base.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    titleMedium = base.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    titleSmall = base.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    // Texto normal
    bodyLarge = base.copy(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    bodyMedium = base.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall = base.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    // Botones, chips, etiquetas
    labelLarge = base.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    labelMedium = base.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp),
    labelSmall = base.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp)
)
