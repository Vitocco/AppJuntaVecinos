package com.example.appjuntavecinos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.model.Informe
import com.example.appjuntavecinos.model.Movimiento


//Resultado de la consulta agrupada
data class GastoCategoria(val categoria: String, val total: Int)

@Dao
interface FinanzasDao {
    @Insert
    suspend fun insertarMovimiento(movimiento: Movimiento)

    @Insert
    suspend fun insertarInforme(informe: Informe)

    //suma de ingreso o gasto dentro de un mes
    @Query("SELECT COALESCE(SUM(monto), 0) FROM movimientos WHERE tipo = :tipo AND fecha >= :desde AND fecha < :hasta")
    suspend fun totalDelMes(tipo: String, desde: Long, hasta: Long): Int

    //gastos del mes agrupados por categoria
    @Query("SELECT categoria, SUM(monto) AS total FROM movimientos WHERE tipo = 'GASTO' AND fecha >= :desde AND fecha < :hasta GROUP BY categoria ORDER BY total DESC")
    suspend fun gastosPorCategoria(desde: Long, hasta: Long): List<GastoCategoria>

    //neto acumulado
    @Query("SELECT COALESCE(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END), 0) FROM movimientos WHERE fecha < :hasta")
    suspend fun netoHasta(hasta: Long): Int

    @Query("SELECT * FROM informes ORDER BY fechaPublicacion DESC")
    suspend fun informes(): List<Informe>

    @Query("SELECT COUNT(*) FROM movimientos")
    suspend fun contarMovimientos(): Int

}
