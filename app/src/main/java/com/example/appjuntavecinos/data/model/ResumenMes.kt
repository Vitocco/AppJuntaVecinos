package com.example.appjuntavecinos.data.model

data class  ResumenMes(
    val ingresos: Int,
    val gastos: Int,
    val saldo: Int,
    val gastosPorCategoria: List<GastoCategoria>
)
