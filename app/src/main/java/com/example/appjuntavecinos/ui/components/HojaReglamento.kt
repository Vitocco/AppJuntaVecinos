package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.GrisCalido
import kotlinx.coroutines.launch

/**
 * Hoja inferior (ModalBottomSheet) con el reglamento de uso de sedes.
 * El texto vive en res/values/strings.xml. "Entendido" cierra la hoja con su
 * animación y luego avisa (el registro marca la casilla "Acepto el reglamento").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaReglamento(onEntendido: () -> Unit, onCerrar: () -> Unit) {
    // skipPartiallyExpanded: se abre completa, no a medias
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val alcance = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = CarbonCampo
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            TextoConPunto(stringResource(R.string.reglamento_titulo), estilo = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))

            // Texto largo con scroll propio (máx. 60% de una pantalla típica)
            Text(
                text = stringResource(R.string.reglamento_texto),
                style = MaterialTheme.typography.bodyMedium,
                color = GrisCalido,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            )

            Spacer(Modifier.height(18.dp))
            BotonPrincipal(
                texto = "Entendido",
                onClick = {
                    // primero se oculta la hoja (animación) y después se avisa
                    alcance.launch { estadoHoja.hide() }.invokeOnCompletion { onEntendido() }
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
