package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.AuthResponse
import com.example.appjuntavecinos.data.remote.dto.LoginRequest
import com.example.appjuntavecinos.data.remote.dto.RegisterRequest
import com.example.appjuntavecinos.data.remote.llamarApi
import com.example.appjuntavecinos.data.session.Sesion
import com.example.appjuntavecinos.data.session.SesionManager

class AuthRepository(private val api: ApiService) {

    suspend fun login(email: String, password: String): Resultado<AuthResponse> {
        val resultado = llamarApi { api.login(LoginRequest(email.trim(), password)) }
        if (resultado is Resultado.Exito) guardarSesion(resultado.datos)
        return resultado
    }

    // El backend responde igual que el login (rol siempre VECINO): quedas logueado.
    suspend fun registrar(datos: RegisterRequest): Resultado<AuthResponse> {
        val resultado = llamarApi { api.registrar(datos) }
        if (resultado is Resultado.Exito) guardarSesion(resultado.datos)
        return resultado
    }

    suspend fun cerrarSesion() = SesionManager.cerrar()

    private suspend fun guardarSesion(r: AuthResponse) =
        SesionManager.guardar(Sesion(r.token, r.id, r.nombre, r.email, r.rol))
}
