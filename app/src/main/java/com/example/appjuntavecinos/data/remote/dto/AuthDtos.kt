package com.example.appjuntavecinos.data.remote.dto

// =====================================================================
// DTOs (Data Transfer Objects): copias EXACTAS del JSON del backend (ver API.md).
// Los nombres de los campos deben ser idénticos a los del JSON porque Gson
// los empareja por nombre. Fechas "yyyy-MM-dd", horas "HH:mm", montos Long.
// =====================================================================

// POST api/auth/login
data class LoginRequest(
    val email: String,
    val password: String
)

// POST api/auth/register  (telefono y numeroCasa son String)
data class RegisterRequest(
    val nombre: String,
    val email: String,
    val telefono: String,        // "+56912345678"
    val numeroCasa: String,
    val fechaNacimiento: String, // "1990-04-12"
    val password: String,
    val recibirAvisos: Boolean
)

// Respuesta de login y de register
data class AuthResponse(
    val token: String,
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String              // "VECINO" | "DIRECTIVA" | "TESORERO"
)

// Cuerpo de TODOS los errores del backend (400/401/403/404/409)
data class ErrorDto(
    val status: Int?,
    val error: String?,
    val mensaje: String?,
    val timestamp: String?
)

// GET / PUT api/usuarios/me
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val telefono: String?,
    val numeroCasa: String?,
    val fechaNacimiento: String?,
    val recibirAvisos: Boolean
)

data class ActualizarUsuarioRequest(
    val nombre: String,
    val telefono: String,
    val numeroCasa: String,
    val recibirAvisos: Boolean
)
