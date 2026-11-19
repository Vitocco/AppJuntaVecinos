package com.example.appjuntavecinos.data.repository

import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.ActualizarUsuarioRequest
import com.example.appjuntavecinos.data.remote.dto.UsuarioDto
import com.example.appjuntavecinos.data.remote.llamarApi
import com.example.appjuntavecinos.data.session.SesionManager

// Perfil del usuario logueado (GET / PUT api/usuarios/me)
class UsuarioRepository(private val api: ApiService) {

    suspend fun obtenerPerfil(): Resultado<UsuarioDto> = llamarApi { api.miPerfil() }

    suspend fun actualizarPerfil(datos: ActualizarUsuarioRequest): Resultado<UsuarioDto> {
        val resultado = llamarApi { api.actualizarPerfil(datos) }
        // El saludo "Hola, <nombre>" sale de la sesión: se actualiza también ahí
        if (resultado is Resultado.Exito) SesionManager.actualizarNombre(resultado.datos.nombre)
        return resultado
    }
}
