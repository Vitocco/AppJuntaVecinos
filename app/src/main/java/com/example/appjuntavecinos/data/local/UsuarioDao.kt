package com.example.appjuntavecinos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.data.model.Usuario


@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?
}
