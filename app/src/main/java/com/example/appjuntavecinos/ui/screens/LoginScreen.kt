package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.BotonPrincipal
import com.example.appjuntavecinos.ui.components.CampoTexto
import com.example.appjuntavecinos.ui.components.CasasFondo
import com.example.appjuntavecinos.ui.components.DosColumnas
import com.example.appjuntavecinos.ui.components.FondoZaror
import com.example.appjuntavecinos.ui.components.LogoZaror
import com.example.appjuntavecinos.ui.components.MensajeError
import com.example.appjuntavecinos.ui.components.MensajeExito
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.esVertical
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

/**
 * Login (diseño 1): logo, "Bienvenido a / Junta Zaror.", correo, contraseña con ojo,
 * botón crema "Entrar", separador "¿Aún no tienes cuenta?", "Crear cuenta" y casas al pie.
 * No hay selector de rol: el rol lo define la cuenta.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String) -> Unit = {},
    onRegisterClick: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val formulario by viewModel.formulario.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    // Cuando el login es correcto se muestra el cartel y luego se navega según el rol
    var rolExito by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(rolExito) {
        rolExito?.let {
            delay(1200)
            onLoginClick(it)
        }
    }

    FondoZaror(modifier) {
        // Casas al pie (solo vertical: en horizontal taparían el formulario)
        if (esVertical()) {
            CasasFondo(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            DosColumnas(
                espacio = 0.dp,
                izquierda = {
                    // Los ~36dp que ocupaba el subtítulo se repartieron arriba y abajo del título
                    Spacer(Modifier.height(40.dp))
                    LogoZaror()
                    Spacer(Modifier.height(56.dp))
                    Text("Bienvenido a", style = MaterialTheme.typography.bodyLarge, color = GrisCalido)
                    Spacer(Modifier.height(6.dp))
                    TextoConPunto("Junta\nZaror", estilo = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(44.dp))
                },
                derecha = {
                    MensajeExito(visible = rolExito != null, texto = "¡Inicio de sesión exitoso!")
                    if (rolExito != null) Spacer(Modifier.height(16.dp))

                    CampoTexto(
                        etiqueta = "Correo electrónico",
                        valor = formulario.email,
                        onValorChange = viewModel::actualizarEmail,
                        placeholder = "nombre@correo.com",
                        icono = Icons.Outlined.Email,
                        tipoTeclado = KeyboardType.Email,
                        error = errores.email
                    )
                    Spacer(Modifier.height(16.dp))
                    CampoTexto(
                        etiqueta = "Contraseña",
                        valor = formulario.password,
                        onValorChange = viewModel::actualizarPassword,
                        placeholder = "Tu contraseña",
                        icono = Icons.Outlined.Lock,
                        esPassword = true,
                        error = errores.password
                    )
                    Spacer(Modifier.height(16.dp))
                    // Error del backend (credenciales) o "No se pudo conectar con el servidor"
                    MensajeError(mensajeError, modifier = Modifier.padding(bottom = 8.dp))
                    Spacer(Modifier.height(8.dp))
                    BotonPrincipal(
                        texto = "Entrar",
                        cargando = cargando || rolExito != null,
                        onClick = { viewModel.iniciarSesion(onExito = { rolExito = it }) }
                    )
                    Spacer(Modifier.height(24.dp))

                    // ---- separador "¿Aún no tienes cuenta?" ----
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(Modifier.weight(1f), color = CarbonCampo)
                        Text(
                            "¿Aún no tienes cuenta?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrisCalido,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(Modifier.weight(1f), color = CarbonCampo)
                    }
                    TextButton(
                        onClick = onRegisterClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Crear cuenta", style = MaterialTheme.typography.titleMedium, color = Terracota)
                    }
                }
            )
            // espacio para que las casas no tapen "Crear cuenta"
            if (esVertical()) Spacer(Modifier.height(130.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    AppJuntaVecinosTheme { LoginScreen() }
}
