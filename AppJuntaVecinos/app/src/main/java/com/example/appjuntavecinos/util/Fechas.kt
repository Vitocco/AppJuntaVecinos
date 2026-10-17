package com.example.appjuntavecinos.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Fechas {
    private val local = Locale.forLanguageTag("es-CL")

    // Hoy y los días siguientes, cada uno como "inicio del día" en milisegundos
    fun proximosDias(cantidad: Int = 7): List<Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return List(cantidad) { cal.timeInMillis.also { cal.add(Calendar.DAY_OF_YEAR, 1) } }
    }

    fun nombreDia(m: Long) = SimpleDateFormat("EEE", local).format(Date(m))
        .replaceFirstChar { it.uppercase() }.removeSuffix(".")
    fun numeroDia(m: Long) = SimpleDateFormat("d", local).format(Date(m))
    fun mes(m: Long) = SimpleDateFormat("MMMM", local).format(Date(m))
        .replaceFirstChar { it.uppercase() }
    fun hora(h: Int) = "%02d:00".format(h)

    fun fecha(anio: Int, mes: Int, dia: Int): Long =
        Calendar.getInstance().apply { clear(); set(anio, mes - 1, dia) }.timeInMillis

    fun etiquetaMes(anio: Int, mes: Int): String =
        SimpleDateFormat("MMM yyyy", local).format(Date(fecha(anio, mes, 1)))
            .replace(".", "").replace("sept", "sep").replaceFirstChar { it.uppercase() }

    fun diaCorto(m: Long): String =
        SimpleDateFormat("d MMM", local).format(Date(m)).replace(".", "").replace("sept", "sep")

    // 105000 -> "$105.000"
    fun moneda(monto: Int): String = "\$" + "%,d".format(local, monto)
}