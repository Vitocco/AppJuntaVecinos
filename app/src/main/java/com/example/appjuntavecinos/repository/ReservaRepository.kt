package com.example.appjuntavecinos.repository


import com.example.appjuntavecinos.data.ReservaDao
import com.example.appjuntavecinos.model.Reserva

class ReservaRepository(private val dao: ReservaDao) {
    suspend fun buscarChoques(fecha: Long, inicio: Int, termino: Int) =
        dao.buscarChoques(fecha, inicio, termino)
    suspend fun guardar(reserva: Reserva) = dao.insertar(reserva)
}