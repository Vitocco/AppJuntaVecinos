package com.example.appjuntavecinos

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.session.SesionManager

/**
 * Clase Application: se crea ANTES que cualquier pantalla (registrada en el Manifest).
 *  - Carga la sesión guardada en DataStore.
 *  - Le dice a Coil que use nuestro OkHttp, así las imágenes protegidas
 *    (comprobantes) también llevan el token.
 */
class JuntaZarorApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        SesionManager.iniciar(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient { RetrofitClient.okHttp }
            .crossfade(true)
            .build()
}
