package com.example.appjuntavecinos.data.repository


import com.example.appjuntavecinos.data.local.ReservaDao
import com.example.appjuntavecinos.data.model.Reserva

class ReservaRepository(private val dao: ReservaDao) {
    suspend fun buscarChoques(fecha: Long, inicio: Int, termino: Int) =
        dao.buscarChoques(fecha, inicio, termino)
    suspend fun guardar(reserva: Reserva) = dao.insertar(reserva)
}