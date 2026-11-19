package com.example.appjuntavecinos.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

// Ayudas para armar las peticiones multipart (POST movimientos y POST pagos).
object Multipart {

    /** Parte "datos": el objeto convertido a JSON con Content-Type application/json. */
    fun datosJson(objeto: Any): RequestBody =
        RetrofitClient.gson.toJson(objeto).toRequestBody("application/json".toMediaType())

    /** Parte "archivo": los bytes del JPG comprimido. */
    fun archivoJpg(bytes: ByteArray, nombre: String = "comprobante.jpg"): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            "archivo",
            nombre,
            bytes.toRequestBody("image/jpeg".toMediaType())
        )
}
