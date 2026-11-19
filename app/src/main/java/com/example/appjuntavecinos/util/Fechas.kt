package com.example.appjuntavecinos.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Fechas y horas. El backend usa texto: fechas "yyyy-MM-dd" y horas "HH:mm".
 * Aquí se convierten a LocalDate / LocalTime (java.time) y a textos en español
 * como los del diseño: "Sáb 22 nov", "Noviembre 2026", "18:00 a 23:00".
 */
object Fechas {
    private val chile = Locale.forLanguageTag("es-CL")
    private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    fun hoy(): LocalDate = LocalDate.now()
    fun mesActual(): YearMonth = YearMonth.now()

    // ---------------- texto del backend <-> java.time ----------------
    fun leer(fecha: String): LocalDate = LocalDate.parse(fecha)        // "2026-11-22"
    fun aApi(fecha: LocalDate): String = fecha.toString()              // -> "2026-11-22"
    fun leerHora(hora: String): LocalTime = LocalTime.parse(hora)      // "18:00"
    fun horaApi(hora: LocalTime): String = hora.format(formatoHora)    // -> "18:00"

    // ---------------- textos para mostrar ----------------
    // "sáb." -> "Sáb"   /   "nov." -> "nov"
    private fun limpiar(t: String) = t.replace(".", "").replace("sept", "sep")

    fun diaSemanaCorto(f: LocalDate): String =
        limpiar(f.dayOfWeek.getDisplayName(TextStyle.SHORT, chile)).replaceFirstChar { it.uppercase() }

    fun mesCorto(f: LocalDate): String = limpiar(f.month.getDisplayName(TextStyle.SHORT, chile))

    /** "22 nov" */
    fun diaMes(f: LocalDate): String = "${f.dayOfMonth} ${mesCorto(f)}"

    /** "Sáb 22 nov" */
    fun diaCompleto(f: LocalDate): String = "${diaSemanaCorto(f)} ${diaMes(f)}"

    /** "18 nov 2026" */
    fun diaMesAnio(f: LocalDate): String = "${diaMes(f)} ${f.year}"

    /** "Noviembre" */
    fun nombreMes(m: YearMonth): String =
        m.month.getDisplayName(TextStyle.FULL, chile).replaceFirstChar { it.uppercase() }

    /** "Noviembre 2026" */
    fun mesAnio(m: YearMonth): String = "${nombreMes(m)} ${m.year}"

    /** "Nov" (chips de mes) */
    fun mesCortoMayus(m: YearMonth): String =
        limpiar(m.month.getDisplayName(TextStyle.SHORT, chile)).replaceFirstChar { it.uppercase() }

    /** "15:00" + "20:00" -> "15:00 a 20:00" */
    fun horario(inicio: String, fin: String): String = "$inicio a $fin"

    /** "2026-11-03T20:00:00" -> "3 nov" (fecha de publicación de un informe) */
    fun diaMesDeFechaHora(texto: String?): String =
        texto?.let { runCatching { diaMes(LocalDateTime.parse(it).toLocalDate()) }.getOrNull() } ?: ""

    /** Los últimos [cantidad] meses terminando en el actual (para los chips de mes). */
    fun ultimosMeses(cantidad: Int = 6): List<YearMonth> =
        (cantidad - 1 downTo 0).map { mesActual().minusMonths(it.toLong()) }

    // ---------------- DatePicker de Material 3 (trabaja en milisegundos UTC) ----------------
    fun aMillisUtc(f: LocalDate): Long = f.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    fun desdeMillisUtc(ms: Long): LocalDate = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()

    // Horas que se ofrecen al reservar (el reglamento permite 08:00 a 23:59)
    val horasInicio: List<String> = (8..22).map { "%02d:00".format(it) }
    val horasFin: List<String> = (9..23).map { "%02d:00".format(it) } + "23:59"
}
