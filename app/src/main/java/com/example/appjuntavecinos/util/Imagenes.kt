package com.example.appjuntavecinos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Convierte la foto del comprobante (de cámara o galería) en un JPG liviano para
 * subirlo al backend (que acepta máx. 5 MB). Reduce la imagen a 1600 px como máximo
 * por lado y la comprime al 80 %. Corre en un hilo de fondo (Dispatchers.IO).
 */
object Imagenes {

    private const val LADO_MAXIMO = 1600
    private const val CALIDAD_JPG = 80

    suspend fun comprimirAJpg(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        // 1° Leer solo el tamaño (sin cargar la imagen completa en memoria)
        val opcionesTamano = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcionesTamano) }

        // 2° Calcular cuánto achicarla al leerla (potencias de 2: 1, 2, 4...)
        var muestreo = 1
        while (opcionesTamano.outWidth / (muestreo * 2) >= LADO_MAXIMO ||
            opcionesTamano.outHeight / (muestreo * 2) >= LADO_MAXIMO
        ) muestreo *= 2

        // 3° Leer la imagen ya reducida
        val original = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestreo })
        } ?: throw IllegalStateException("No se pudo leer la imagen")

        // 4° Ajuste fino al lado máximo y compresión a JPG
        val escala = minOf(1f, LADO_MAXIMO.toFloat() / maxOf(original.width, original.height))
        val final = if (escala < 1f) {
            Bitmap.createScaledBitmap(original, (original.width * escala).toInt(), (original.height * escala).toInt(), true)
        } else original

        ByteArrayOutputStream().use { salida ->
            final.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPG, salida)
            salida.toByteArray()
        }
    }
}
