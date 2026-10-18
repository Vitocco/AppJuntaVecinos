package com.example.appjuntavecinos.repository


import com.example.appjuntavecinos.data.FinanzasDao
import com.example.appjuntavecinos.data.GastoCategoria
import com.example.appjuntavecinos.model.Informe
import com.example.appjuntavecinos.model.Movimiento
import com.example.appjuntavecinos.util.Fechas

data class ResumenMes(
    val ingresos: Int,
    val gastos: Int,
    val saldo: Int,
    val gastosPorCategoria: List<GastoCategoria>
)

class FinanzasRepository(private val dao: FinanzasDao) {

    companion object {
        const val SALDO_APERTURA = 331_300
    }

    suspend fun resumen(anio: Int, mes: Int): ResumenMes {
        val desde = Fechas.fecha(anio, mes, 1)
        val hasta = if (mes == 12) Fechas.fecha(anio + 1, 1, 1) else Fechas.fecha(anio, mes + 1, 1)
        return ResumenMes(
            ingresos = dao.totalDelMes("INGRESO", desde, hasta),
            gastos = dao.totalDelMes("GASTO", desde, hasta),
            saldo = SALDO_APERTURA + dao.netoHasta(hasta),
            gastosPorCategoria = dao.gastosPorCategoria(desde, hasta)
        )
    }

    suspend fun informes(): List<Informe> = dao.informes()

    suspend fun sembrarSiVacio() {
        if (dao.contarMovimientos() > 0) return

        val movimientos = listOf(
            Movimiento(fecha = Fechas.fecha(2026, 9, 6),  tipo = "INGRESO", categoria = "Arriendo", monto = 35_000),
            Movimiento(fecha = Fechas.fecha(2026, 9, 20), tipo = "INGRESO", categoria = "Arriendo", monto = 35_000),
            Movimiento(fecha = Fechas.fecha(2026, 9, 10), tipo = "GASTO", categoria = "Luz",  monto = 19_800),
            Movimiento(fecha = Fechas.fecha(2026, 9, 12), tipo = "GASTO", categoria = "Gas",  monto = 14_000),
            Movimiento(fecha = Fechas.fecha(2026, 9, 14), tipo = "GASTO", categoria = "Agua", monto = 11_500),
            Movimiento(fecha = Fechas.fecha(2026, 10, 4),  tipo = "INGRESO", categoria = "Arriendo", monto = 35_000),
            Movimiento(fecha = Fechas.fecha(2026, 10, 11), tipo = "INGRESO", categoria = "Arriendo", monto = 35_000),
            Movimiento(fecha = Fechas.fecha(2026, 10, 18), tipo = "INGRESO", categoria = "Arriendo", monto = 35_000),
            Movimiento(fecha = Fechas.fecha(2026, 10, 8),  tipo = "GASTO", categoria = "Luz",  monto = 21_400),
            Movimiento(fecha = Fechas.fecha(2026, 10, 9),  tipo = "GASTO", categoria = "Gas",  monto = 15_000),
            Movimiento(fecha = Fechas.fecha(2026, 10, 10), tipo = "GASTO", categoria = "Agua", monto = 12_300)
        )
        movimientos.forEach { dao.insertarMovimiento(it) }

        dao.insertarInforme(Informe(titulo = "Informe septiembre 2026", fechaPublicacion = Fechas.fecha(2026, 10, 1)))
        dao.insertarInforme(Informe(titulo = "Informe agosto 2026", fechaPublicacion = Fechas.fecha(2026, 9, 2)))
        dao.insertarInforme(Informe(titulo = "Rendición 1er semestre 2026", fechaPublicacion = Fechas.fecha(2026, 7, 15), esRendicion = true))
    }
}