package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.viewmodel.EstadoUi

/**
 * Pantalla con lista (Inicio, Reservas, Movimientos...): fondo Zaror, márgenes de 24dp,
 * respeta la barra de estado / teclado y permite "deslizar hacia abajo para actualizar".
 * [botonFlotante] se dibuja abajo a la derecha (ej: "+" para crear).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    refrescando: Boolean = false,
    onRefrescar: (() -> Unit)? = null,
    botonFlotante: (@Composable () -> Unit)? = null,
    contenido: LazyListScope.() -> Unit
) {
    FondoZaror {
        val lista = @Composable {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = contenido
            )
        }
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            if (onRefrescar != null) {
                PullToRefreshBox(isRefreshing = refrescando, onRefresh = onRefrescar, modifier = Modifier.fillMaxSize()) { lista() }
            } else {
                lista()
            }
            if (botonFlotante != null) {
                Box(Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(20.dp)) { botonFlotante() }
            }
        }
    }
}

/**
 * Dibuja una sección de la lista según su estado:
 *  - Cargando -> spinner
 *  - Error    -> mensaje del backend + "Reintentar"
 *  - Listo    -> aviso "Sin conexión" si vino de Room, y luego [contenido] con los datos
 */
fun <T> LazyListScope.seccion(
    estado: EstadoUi<T>,
    onReintentar: () -> Unit,
    contenido: LazyListScope.(datos: T) -> Unit
) {
    when (estado) {
        EstadoUi.Cargando -> item { EstadoCarga() }
        is EstadoUi.Error -> item { EstadoError(estado.mensaje, onReintentar) }
        is EstadoUi.Listo -> {
            if (estado.desdeCache) item { AvisoSinConexion() }
            contenido(estado.datos)
        }
    }
}

/** Pantalla de formulario (scroll vertical simple, sin barra inferior). */
@Composable
fun PantallaFormulario(contenido: @Composable ColumnScope.() -> Unit) {
    FondoZaror {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = contenido
        )
    }
}
