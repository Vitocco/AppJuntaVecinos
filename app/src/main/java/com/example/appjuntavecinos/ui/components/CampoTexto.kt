package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.CarbonCampoOn
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.Terracota

/**
 * Campo de texto del diseño Zaror:
 *  - etiqueta ARRIBA del campo (no flotante),
 *  - fondo relleno CarbonCampo, sin borde; al enfocarlo, borde terracota,
 *  - el mensaje de error aparece DEBAJO del campo (rojo). Si no hay error, se puede
 *    mostrar un texto de ayuda gris.
 *
 * Con [esPassword] agrega el "ojo" para mostrar/ocultar la contraseña.
 * Con [onClick] el campo es solo de lectura y se comporta como un botón
 * (se usa para abrir el calendario o el selector de hora).
 */
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    icono: ImageVector? = null,
    error: String? = null,
    ayuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esPassword: Boolean = false,
    unaLinea: Boolean = true,
    minLineas: Int = 1,
    prefijo: String? = null,
    sufijo: String? = null,
    estiloTexto: TextStyle = MaterialTheme.typography.bodyLarge,
    transformacion: VisualTransformation = VisualTransformation.None,   // ej: SeparadorMiles
    onClick: (() -> Unit)? = null
) {
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }

    // Si el campo es "clickeable" (fecha, hora) detectamos el toque con su InteractionSource.
    val interaccion = remember { MutableInteractionSource() }
    val accionClick by rememberUpdatedState(onClick)
    if (onClick != null) {
        LaunchedEffect(interaccion) {
            interaccion.interactions.collect { evento ->
                if (evento is PressInteraction.Release) accionClick?.invoke()
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (etiqueta.isNotEmpty()) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = GrisCalido,
                modifier = Modifier.padding(start = 2.dp)
            )
        }

        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = onClick != null,
            singleLine = unaLinea,
            minLines = minLineas,
            textStyle = estiloTexto,
            isError = error != null,
            interactionSource = interaccion,
            shape = MaterialTheme.shapes.medium,
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, style = estiloTexto) }
            } else null,
            leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
            prefix = prefijo?.let { { Text(it, style = estiloTexto, color = GrisCalido) } },
            suffix = sufijo?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
            trailingIcon = if (esPassword) {
                {
                    IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                        Icon(
                            imageVector = if (mostrarPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            } else null,
            visualTransformation = when {
                esPassword && !mostrarPassword -> PasswordVisualTransformation()
                esPassword -> VisualTransformation.None
                else -> transformacion
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esPassword) KeyboardType.Password else tipoTeclado
            ),
            colors = coloresCampo()
        )

        // Error (rojo) o ayuda (gris) debajo del campo
        when {
            error != null -> Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = RojoError,
                modifier = Modifier.padding(start = 4.dp)
            )
            ayuda != null -> Text(
                text = ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = GrisCalido,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// Colores del campo: relleno carbón, borde invisible, borde terracota al enfocar y rojo con error.
@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CarbonCampoOn,
    unfocusedContainerColor = CarbonCampo,
    disabledContainerColor = CarbonCampo,
    errorContainerColor = CarbonCampo,
    focusedBorderColor = Terracota,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = RojoError,
    focusedTextColor = Crema,
    unfocusedTextColor = Crema,
    disabledTextColor = Crema,
    errorTextColor = Crema,
    cursorColor = Terracota,
    errorCursorColor = RojoError,
    focusedLeadingIconColor = Terracota,
    unfocusedLeadingIconColor = GrisCalido,
    errorLeadingIconColor = RojoError,
    focusedTrailingIconColor = GrisCalido,
    unfocusedTrailingIconColor = GrisCalido,
    errorTrailingIconColor = GrisCalido,
    focusedPlaceholderColor = GrisCalido,
    unfocusedPlaceholderColor = GrisCalido,
    errorPlaceholderColor = GrisCalido,
    focusedSuffixColor = GrisCalido,
    unfocusedSuffixColor = GrisCalido,
    errorSuffixColor = GrisCalido,
    focusedPrefixColor = GrisCalido,
    unfocusedPrefixColor = GrisCalido,
    errorPrefixColor = GrisCalido
)
