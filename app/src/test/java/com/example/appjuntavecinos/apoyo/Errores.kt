package com.example.appjuntavecinos.apoyo

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** Error HTTP como lo lanza Retrofit, con el JSON {status, error, mensaje} del backend. */
fun errorHttp(codigo: Int, mensaje: String? = null): HttpException {
    val cuerpo = if (mensaje != null) """{"status":$codigo,"mensaje":"$mensaje"}""" else ""
    return HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))
}
