package com.example.appjuntavecinos.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CamaraUriFactory {
    fun crearUri(context: Context): Uri {
        val dir = File(context.filesDir, "comprobantes")
        if (!dir.exists() && !dir.mkdirs()) {
            throw IllegalStateException("No se pudo crear el directorio de comprobantes")
        }
        val foto = File.createTempFile("comprobante_", ".jpg", dir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            foto
        )
    }
}

