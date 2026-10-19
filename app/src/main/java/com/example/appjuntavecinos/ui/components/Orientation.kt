package com.example.appjuntavecinos.ui.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun esVertical(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

@Composable
fun esHorizontal(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

@Composable
fun SegunOrientacion(
    vertical: @Composable () -> Unit,
    horizontal: @Composable () -> Unit
) {
    if (esHorizontal()) horizontal() else vertical()
}