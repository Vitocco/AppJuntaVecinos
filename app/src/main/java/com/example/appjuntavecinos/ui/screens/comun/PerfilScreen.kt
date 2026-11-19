package com.example.appjuntavecinos.ui.screens.comun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.BadgeRol
import com.example.appjuntavecinos.ui.components.BotonPrincipal
import com.example.appjuntavecinos.ui.components.BotonTonal
import com.example.appjuntavecinos.ui.components.CampoTexto
import com.example.appjuntavecinos.ui.components.CasillaVerificacion
import com.example.appjuntavecinos.ui.components.EstadoCarga
import com.example.appjuntavecinos.ui.components.EstadoError
import com.example.appjuntavecinos.ui.components.MostrarMensaje
import com.example.appjuntavecinos.ui.components.PantallaFormulario
import com.example.appjuntavecinos.ui.components.Tarjeta
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.Tono
import com.example.appjuntavecinos.ui.theme.AmbarFondo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.TerracotaSuave
import com.example.appjuntavecinos.ui.viewmodel.EstadoUi
import com.example.appjuntavecinos.ui.viewmodel.PerfilViewModel
import com.example.appjuntavecinos.util.Fechas

/** Perfil (los 3 roles): ver y editar mis datos (GET / PUT usuarios/me) y cerrar sesión. */
@Composable
fun PerfilScreen(viewModel: PerfilViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val form by viewModel.form.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val sesion by viewModel.sesion.collectAsState()

    MostrarMensaje(mensaje, viewModel::mensajeMostrado)

    PantallaFormulario {
        TextoConPunto("Mi perfil", estilo = MaterialTheme.typography.displaySmall)

        when (val e = estado) {
            EstadoUi.Cargando -> EstadoCarga()
            is EstadoUi.Error -> EstadoError(e.mensaje, onReintentar = viewModel::cargar)
            is EstadoUi.Listo -> {
                val usuario = e.datos
                // Cabecera: avatar con inicial, nombre, correo y rol
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(56.dp).clip(CircleShape).background(AmbarFondo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                usuario.nombre.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TerracotaSuave
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(usuario.nombre, style = MaterialTheme.typography.titleLarge, color = Crema)
                            Text(usuario.email, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
                            val nacimiento = usuario.fechaNacimiento?.let { "Nació el ${Fechas.diaMesAnio(Fechas.leer(it))}" }
                            if (nacimiento != null) Text(nacimiento, style = MaterialTheme.typography.bodySmall, color = GrisCalido)
                        }
                        sesion?.rol?.let { BadgeRol(it) }
                    }
                }

                CampoTexto(
                    etiqueta = "Nombre completo",
                    valor = form.nombre,
                    onValorChange = viewModel::actualizarNombre,
                    icono = Icons.Outlined.Person,
                    error = errores.nombre
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    CampoTexto(
                        etiqueta = "Teléfono",
                        valor = form.telefono,
                        onValorChange = viewModel::actualizarTelefono,
                        prefijo = "+56 ",
                        placeholder = "9 1234 5678",
                        tipoTeclado = KeyboardType.Phone,
                        error = errores.telefono,
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        etiqueta = "N° casa / depto",
                        valor = form.numeroCasa,
                        onValorChange = viewModel::actualizarNumeroCasa,
                        tipoTeclado = KeyboardType.Number,
                        error = errores.numeroCasa,
                        modifier = Modifier.weight(1f)
                    )
                }
                CasillaVerificacion(
                    texto = "Recibir avisos de mis reservas",
                    marcada = form.recibirAvisos,
                    onCambio = viewModel::actualizarRecibirAvisos
                )
                BotonPrincipal("Guardar cambios", onClick = viewModel::guardar, cargando = guardando)
            }
        }

        // Cerrar sesión siempre visible (aunque el perfil no cargue)
        BotonTonal(
            texto = "Cerrar sesión",
            onClick = viewModel::cerrarSesion,
            colorTexto = Tono.ERROR.texto,
            colorFondo = Tono.ERROR.fondo
        )
        Spacer(Modifier.size(80.dp))
    }
}
