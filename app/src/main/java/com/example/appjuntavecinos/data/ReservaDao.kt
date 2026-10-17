package com.example.appjuntavecinos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.model.Reserva

@Dao
interface ReservaDao {
    @Insert
    suspend fun insertar(reserva: Reserva)

    // Chocan si cada una empieza antes de que la otra termine
    @Query("""
        SELECT * FROM reservas
        WHERE fecha = :fecha AND estado != 'RECHAZADA'
          AND horaInicio < :termino AND :inicio < horaTermino
        ORDER BY horaInicio
    """)
    suspend fun buscarChoques(fecha: Long, inicio: Int, termino: Int): List<Reserva>
}