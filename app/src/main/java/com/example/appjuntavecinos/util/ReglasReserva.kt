package com.example.appjuntavecinos.util

import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import java.time.LocalDate

// Reglas de negocio sobre una reserva, en UN solo lugar para que todas las pantallas
// decidan igual qué botones mostrar. (El backend vuelve a validar todo igualmente.)

val ReservaDto.dia: LocalDate get() = Fechas.leer(fecha)

/** Hoy o después. */
val ReservaDto.esFutura: Boolean get() = !dia.isBefore(Fechas.hoy())

/** El dueño puede cancelar si está PENDIENTE o APROBADA, no pagada y aún no pasa (reglamento art. 6). */
val ReservaDto.sePuedeCancelar: Boolean
    get() = (estado == "PENDIENTE" || estado == "APROBADA") && !pagada && esFutura

/** Aprobada, no pagada y vigente: se puede pagar (Webpay vecino o registro del tesorero). */
val ReservaDto.porPagar: Boolean
    get() = estado == "APROBADA" && !pagada

/** "Sáb 22 nov · 18:00 a 23:00" */
val ReservaDto.cuando: String
    get() = "${Fechas.diaCompleto(dia)} · ${Fechas.horario(horaInicio, horaFin)}"

/** Orden cronológico (fecha y luego hora de inicio). */
val ordenCronologico = compareBy<ReservaDto>({ it.fecha }, { it.horaInicio })
