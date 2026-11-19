package com.example.appjuntavecinos.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Muestra "35000" como "35.000" mientras se escribe el monto.
 * Solo cambia lo que se VE: el ViewModel sigue guardando "35000" (solo dígitos).
 * OffsetMapping traduce la posición del cursor entre ambos textos.
 */
object SeparadorMiles : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val largo = digitos.length
        // ¿Va un punto ANTES del dígito i? (cada 3 dígitos contando desde la derecha)
        fun puntoAntes(i: Int) = i > 0 && (largo - i) % 3 == 0

        val conPuntos = buildString {
            digitos.forEachIndexed { i, c -> if (puntoAntes(i)) append('.'); append(c) }
        }

        val mapeo = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + (1 until offset).count { puntoAntes(it) }

            override fun transformedToOriginal(offset: Int): Int =
                (0..largo).last { originalToTransformed(it) <= offset }
        }
        return TransformedText(AnnotatedString(conPuntos), mapeo)
    }
}
