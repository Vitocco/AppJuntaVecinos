package com.example.appjuntavecinos.repository

import com.example.appjuntavecinos.data.UsuarioDao
import com.example.appjuntavecinos.model.Usuario
import com.example.appjuntavecinos.util.Seguridad

// Intermediario entre el ViewModel y el DAO (igual que NoteRepository.kt del profe)
class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun registrar(usuario: Usuario) {
        dao.insertar(usuario)
    }

    suspend fun buscarPorEmail(email: String): Usuario? {
        return dao.buscarPorEmail(email.trim())
    }

    // Socio de prueba precargado: socio@zaror.cl / 1234
    suspend fun crearSocioDePrueba() {
        if (dao.buscarPorEmail("socio@zaror.cl") == null) {
            dao.insertar(
                Usuario(
                    nombre = "Socio de prueba",
                    email = "socio@zaror.cl",
                    telefono = "900000000",
                    numeroCasa = 1,
                    fechaNacimiento = 0L,
                    passwordHash = Seguridad.sha256("1234"),
                    rol = "SOCIO",
                    recibirAvisos = true
                )
            )
        }
    }
}
