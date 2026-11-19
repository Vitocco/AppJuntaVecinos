package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.data.session.SesionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Se ejecuta en CADA petición HTTP (de Retrofit y de Coil):
 *  1. Si hay sesión, agrega "Authorization: Bearer <token>" (solo hacia nuestro backend).
 *  2. Si el backend responde 401 a una petición que llevaba token, el token venció
 *     (dura 8 h): se cierra la sesión y la navegación vuelve sola al login.
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = SesionManager.token

        val peticion = if (token != null && original.url.host == ApiConfig.HOST) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val respuesta = chain.proceed(peticion)

        // 401 con token = sesión vencida o inválida. (Un 401 SIN token es, por
        // ejemplo, "Email o contraseña incorrectos" en el login: eso no cierra nada.)
        if (respuesta.code == 401 && peticion.header("Authorization") != null) {
            SesionManager.expirar()
        }
        return respuesta
    }
}
