package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

// ─────────────────────────────────────────────────────────────
// 1. PARÁMETROS
// Solo lo que hace que el usuario SALGA de esta pantalla.
// "= {}" permite llamar a LoginScreen() sin pasar nada (útil para el Preview).
// Más adelante, la navegación les pasará la acción real ("ir a Inicio").
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class) // el selector segmentado es un componente "nuevo" de Material 3
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,     // permite que MainActivity ajuste la pantalla desde afuera
    onLoginClick: () -> Unit = {},     // se toca "Ingresar"
    onSignUpClick: () -> Unit = {}     // se toca "Regístrate"
) {
    // ─────────────────────────────────────────────────────────
    // 2. ESTADO INTERNO
    // "remember" hace que el valor NO se pierda cada vez que la pantalla se redibuja.
    // "mutableStateOf" hace que, al cambiar el valor, la pantalla se redibuje sola.
    // ⚠️ Temporal: cuando hagamos el LoginViewModel, este estado se muda allá.
    // ─────────────────────────────────────────────────────────
    val roles = listOf("Vecino", "Socio")
    var rolSeleccionado by remember { mutableStateOf(0) }       // 0 = Vecino, 1 = Socio (posición en la lista)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }   // el "ojito": false = oculta

    // ─────────────────────────────────────────────────────────
    // 3. ESTRUCTURA: Surface (fondo) → Column (pila vertical)
    // ─────────────────────────────────────────────────────────
    Surface(
        modifier = modifier.fillMaxSize(),          // usamos el "modifier" recibido, no uno nuevo, para respetar lo que mande MainActivity
        color = MaterialTheme.colorScheme.background // fondo del tema (no un color fijo) → cambia solo si cambias el tema
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()                   // deja espacio para la barra de estado y el teclado
                .verticalScroll(rememberScrollState())  // permite scroll si no cabe
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // ← volvió: todo centrado
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── 4.1 ENCABEZADO: logo + título + subtítulo ──────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))              // PRIMERO recortar las esquinas…
                    .background(MaterialTheme.colorScheme.primary), // …DESPUÉS pintar (si es al revés, el fondo queda cuadrado)
                contentAlignment = Alignment.Center               // centra la casita dentro del cuadrado
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,                       // null = es decorativo, el lector de pantalla lo ignora
                    tint = MaterialTheme.colorScheme.onPrimary,      // "onPrimary" = color que se lee bien ENCIMA de primary
                    modifier = Modifier.size(38.dp)
                )
            }

            Text(
                text = "Junta Zaror",
                fontSize = 26.sp,          // sp (no dp) para texto: respeta si el usuario agrandó la letra del celular
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Arriendos y transparencia de la sede",
                color = MaterialTheme.colorScheme.onSurfaceVariant // gris del tema, para texto secundario
            )

            Spacer(modifier = Modifier.height(8.dp)) // un poco MÁS de aire antes del formulario (se suma a los 16 del spacedBy)

            // ── 4.2 SELECTOR VECINO / SOCIO ───────────────────────────
            // Una fila de botones donde solo uno puede estar activo.
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                // forEachIndexed recorre la lista dándonos la posición (index) y el texto (rol)
                roles.forEachIndexed { index, rol ->
                    SegmentedButton(
                        selected = rolSeleccionado == index,            // ¿este es el elegido?
                        onClick = { rolSeleccionado = index },          // al tocar, cambia el estado → la pantalla se redibuja
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size) // redondea solo los extremos
                    ) {
                        Text(rol)
                    }
                }
            }
            Text(
                text = "Socio: directiva, secretaría o tesorería",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── 4.3 CAMPOS ────────────────────────────────────────────
            OutlinedTextField(
                value = email,                       // lo que se MUESTRA = el estado
                onValueChange = { email = it },      // al escribir, actualizamos el estado ("it" = el texto nuevo)
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // teclado con "@"
                singleLine = true,                   // no permite saltos de línea
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    // botón del ojito: invierte el estado (true ↔ false)
                    IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                        Icon(
                            imageVector = if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                // si mostrarPassword es false → puntitos; si es true → texto normal
                visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── 4.4 BOTONES ───────────────────────────────────────────
            Button(
                onClick = onLoginClick,              // NO decidimos aquí adónde ir: solo avisamos hacia afuera
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)                   // ≥ 48 dp: tamaño cómodo para el dedo
            ) {
                Text("Ingresar", fontSize = 16.sp)
            }

            TextButton(onClick = onSignUpClick) {
                Text("¿No tienes cuenta? Regístrate como vecino")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 5. PREVIEW: una "foto" de la pantalla en Android Studio.
// La envolvemos en el tema para que use los colores de la app.
// ─────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true) // showSystemUi: la muestra dentro de un marco de celular
@Composable
fun LoginScreenPreview() {
    AppJuntaVecinosTheme {
        LoginScreen()   // sin argumentos, gracias a los "= {}" y "= Modifier"
    }
}