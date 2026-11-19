package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.BuildConfig
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Crea UNA sola vez el cliente HTTP y Retrofit (patrón singleton con "object").
 * Los repositorios usan RetrofitClient.api y Coil usa RetrofitClient.okHttp
 * (así las imágenes protegidas también llevan el token).
 */
object RetrofitClient {

    val gson: Gson = Gson()

    val okHttp: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Log de red en Logcat SOLO en debug (etiqueta "OkHttp"). El token nunca queda impreso:
        //  - redactHeader oculta el header Authorization,
        //  - ocultarSecretos tapa "token" (respuesta del login) y "password" en los JSON.
        if (BuildConfig.DEBUG) {
            val log = HttpLoggingInterceptor { linea ->
                Log.d("OkHttp", ocultarSecretos(linea))
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
                redactHeader("Authorization")
            }
            builder.addInterceptor(log)
        }
        builder.build()
    }

    // "token":"eyJhb..." -> "token":"██"   (igual con "password")
    private val camposSecretos = Regex(""""(token|password)"\s*:\s*"[^"]*"""")
    private fun ocultarSecretos(linea: String): String =
        camposSecretos.replace(linea) { "\"${it.groupValues[1]}\":\"██\"" }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
