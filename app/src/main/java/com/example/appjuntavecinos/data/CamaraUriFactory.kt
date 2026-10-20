package com.example.appjuntavecinos.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CamaraUriFactory {

    private fun carpeta(context: Context): File {
        val dir = File(context.filesDir, "comprobantes")
        if (!dir.exists() && !dir.mkdirs()) {
            throw IllegalStateException("No se pudo crear el directorio de comprobantes")
        }
        return dir
    }

    fun crearUri(context: Context): Uri {
        val foto = File.createTempFile("comprobante_", ".jpg", carpeta(context))
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            foto
        )
    }

    // Copia la imagen elegida en la galería a la carpeta de la app,
    // para que el comprobante siga existiendo aunque se cierre la app
    fun copiarDesdeGaleria(context: Context, origen: Uri): Uri {
        val destino = File.createTempFile("comprobante_", ".jpg", carpeta(context))
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: run {
            destino.delete()
            throw IllegalStateException("No se pudo leer la imagen seleccionada")
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destino
        )
    }
}