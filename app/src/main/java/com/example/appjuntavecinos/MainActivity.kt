package com.example.appjuntavecinos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appjuntavecinos.navigation.AppNavigation
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme
import com.example.appjuntavecinos.util.EventosPago

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // ¿Se abrió la app desde el deep link de Webpay (zaror://pago?estado=...)?
        EventosPago.recibir(intent?.data)
        setContent {
            AppJuntaVecinosTheme {
                AppNavigation()
            }
        }
    }

    // La app ya estaba abierta (singleTask): el deep link llega aquí
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        EventosPago.recibir(intent.data)
    }
}
