package com.example.appjuntavecinos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SedeDao {
    @Query("SELECT * FROM cache_sedes ORDER BY id")
    suspend fun obtenerTodas(): List<SedeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sedes: List<SedeEntity>)

    @Query("DELETE FROM cache_sedes")
    suspend fun borrarTodas()

    // Reemplaza la caché completa con lo último que mandó el backend.
    // @Transaction: si algo falla a la mitad, no queda la tabla a medias.
    @Transaction
    suspend fun reemplazar(sedes: List<SedeEntity>) {
        borrarTodas()
        insertar(sedes)
    }
}

@Dao
interface ReservaCacheDao {
    @Query("SELECT * FROM cache_reservas WHERE usuarioId = :usuarioId ORDER BY fecha DESC, horaInicio DESC")
    suspend fun misReservas(usuarioId: Long): List<ReservaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(reservas: List<ReservaEntity>)

    @Query("DELETE FROM cache_reservas WHERE usuarioId = :usuarioId")
    suspend fun borrarDe(usuarioId: Long)

    @Transaction
    suspend fun reemplazarMias(usuarioId: Long, reservas: List<ReservaEntity>) {
        borrarDe(usuarioId)
        insertar(reservas)
    }
}
