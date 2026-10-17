package com.example.appjuntavecinos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appjuntavecinos.navigation.AppNavigation
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // la app dibuja hasta los bordes (debajo de la hora/batería)
        setContent {
            AppJuntaVecinosTheme {
                AppNavigation()
                }
            }
        }
    }
