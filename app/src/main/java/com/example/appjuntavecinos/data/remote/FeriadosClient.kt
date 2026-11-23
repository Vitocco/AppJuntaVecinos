package com.example.appjuntavecinos.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit SEPARADO para la API pública de feriados (Nager.Date).
 * Usa su propio OkHttp SIN AuthInterceptor: esta API no debe recibir el token JWT.
 */
object FeriadosClient {

    const val BASE_URL = "https://date.nager.at/"

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: FeriadoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(RetrofitClient.gson))
            .build()
            .create(FeriadoApi::class.java)
    }
}
