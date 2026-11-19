package com.example.appjuntavecinos.util

import java.util.Locale

// Formato de pesos chilenos. El backend manda los montos como enteros (Long).
object Moneda {
    private val chile = Locale.forLanguageTag("es-CL")

    // 412300 -> "$ 412.300"
    fun pesos(monto: Long): String = "$ " + "%,d".format(chile, monto)

    // Con signo, para ingresos y gastos: "+ $ 105.000" / "− $ 48.700"
    fun ingreso(monto: Long): String = "+ " + pesos(monto)
    fun gasto(monto: Long): String = "− " + pesos(monto)

    // 412300 -> "412.300" (sin el signo $, para el campo de monto grande)
    fun sinSigno(monto: Long): String = "%,d".format(chile, monto)
}
