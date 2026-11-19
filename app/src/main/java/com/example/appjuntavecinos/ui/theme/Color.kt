package com.example.appjuntavecinos.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =====================================================================
// Paleta "Carbón y terracota" de Junta Zaror (tema oscuro único).
// Todas las pantallas sacan sus colores de aquí: si cambia el diseño,
// se cambia en un solo lugar.
// =====================================================================

// ---- Fondos y superficies ----
val Carbon = Color(0xFF1B1917)          // fondo de todas las pantallas
val CarbonCampo = Color(0xFF262220)     // campos de texto y tarjetas
val CarbonCampoOn = Color(0xFF2E2926)   // campo enfocado / tarjeta presionada
val CarbonIcono = Color(0xFF332C28)     // cuadrito detrás de los íconos de las filas
val BarraNav = Color(0xFF15130F)        // barra inferior de pestañas

// ---- Textos ----
val Crema = Color(0xFFF4EFE7)           // texto principal y botón principal
val GrisCalido = Color(0xFFA69C90)      // texto secundario, etiquetas

// ---- Acento ----
val Terracota = Color(0xFFD96A3F)       // botones de acción, chips activos, punto del título
val TerracotaSuave = Color(0xFFE8A27A)  // textos de acento (nombre de la app, "Ver todas")
val TerracotaOscura = Color(0xFFB9512C) // final del degradado de la tarjeta de saldo

// ---- Estados (texto sobre fondo) ----
val VerdeOk = Color(0xFF8DBF82)         // aprobado / pagado / ingreso
val VerdeOkFondo = Color(0xFF2F3A2A)
val RojoError = Color(0xFFE0634A)       // rechazado / gasto / error
val RojoErrorFondo = Color(0xFF3A2626)
val Ambar = Color(0xFFE8A27A)           // pendiente / por pagar
val AmbarFondo = Color(0xFF3A2F22)

// ---- Degradados ----
// Tarjeta de saldo: 135° (de arriba-izquierda a abajo-derecha)
val DegradadoSaldo = Brush.linearGradient(listOf(Terracota, TerracotaOscura))

// Brillo cálido de fondo (arriba a la derecha), terracota con alfa 0.28
val BrilloTerracota = Terracota.copy(alpha = 0.28f)
