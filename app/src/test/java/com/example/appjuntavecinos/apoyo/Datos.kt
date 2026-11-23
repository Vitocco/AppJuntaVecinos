package com.example.appjuntavecinos.apoyo

import com.example.appjuntavecinos.data.remote.dto.AuthResponse
import com.example.appjuntavecinos.data.remote.dto.CategoriaDto
import com.example.appjuntavecinos.data.remote.dto.Feriado
import com.example.appjuntavecinos.data.remote.dto.InformeDto
import com.example.appjuntavecinos.data.remote.dto.MovimientoDto
import com.example.appjuntavecinos.data.remote.dto.OcupacionDto
import com.example.appjuntavecinos.data.remote.dto.PagoDto
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.dto.ResumenDto
import com.example.appjuntavecinos.data.remote.dto.SedeDto
import com.example.appjuntavecinos.data.remote.dto.UsuarioDto
import com.example.appjuntavecinos.data.session.Sesion
import java.time.LocalDate

/** Datos de ejemplo para las pruebas (solo cambia en cada prueba lo que importa). */
object Datos {
    val hoy: LocalDate = LocalDate.now()

    fun reserva(
        id: Long = 1,
        fecha: LocalDate = hoy.plusDays(3),
        estado: String = "APROBADA",
        pagada: Boolean = false,
        precio: Long = 30000,
        sedeId: Long = 1,
        horaInicio: String = "18:00",
        horaFin: String = "22:00"
    ) = ReservaDto(
        id = id, sedeId = sedeId, sedeNombre = "Quincho", sedeFoto = null,
        usuarioId = 7, usuarioNombre = "Camila Rojas", fecha = fecha.toString(),
        horaInicio = horaInicio, horaFin = horaFin, motivo = "Asado vecinal", asistentes = 20,
        estado = estado, pagada = pagada, precio = precio, observacion = null
    )

    fun sede(id: Long = 1, capacidad: Int = 50, activa: Boolean = true) = SedeDto(
        id = id, nombre = "Sede $id", descripcion = null, ubicacion = null,
        capacidad = capacidad, precio = 30000, activa = activa, fotos = listOf("/api/sedes/fotos/a.jpg")
    )

    fun ocupacion(fecha: LocalDate, desde: String, hasta: String) =
        OcupacionDto(fecha.toString(), desde, hasta, "APROBADA")

    val resumen = ResumenDto(saldo = 500000, ingresosMes = 120000, gastosMes = 45000, gastosPorCategoria = emptyList())

    fun movimiento(id: Long, fecha: LocalDate = hoy, tipo: String = "GASTO", monto: Long = 15000) = MovimientoDto(
        id = id, tipo = tipo, categoriaId = 3, categoriaNombre = "Mantención", reservaId = null,
        descripcion = "Ampolletas", monto = monto, fechaMovimiento = fecha.toString(), estado = "VIGENTE",
        usuarioRegistroId = 1, tieneComprobante = false
    )

    fun categoria(id: Long = 3, tipo: String = "GASTO") = CategoriaDto(id, "Mantención", tipo)

    fun informe(id: Long, periodoInicio: String) = InformeDto(
        id = id, titulo = "Rendición $id", periodoInicio = periodoInicio, periodoFin = periodoInicio,
        observacion = null, publicado = true, fechaGeneracion = null, totalIngresos = 0,
        totalGastos = 0, saldoPeriodo = 0, gastosPorCategoria = null
    )

    fun pago(id: Long, estado: String = "PAGADO", fechaPago: String = "2026-11-01") = PagoDto(
        id = id, reservaId = 100 + id, usuarioNombre = "Camila", sedeNombre = "Quincho",
        fechaReserva = null, monto = 30000, fechaPago = fechaPago, medio = "TRANSFERENCIA",
        referencia = "123", estado = estado, movimientoId = null
    )

    fun auth(rol: String = "VECINO") =
        AuthResponse(token = "jwt-123", id = 7, nombre = "Camila Rojas", email = "camila@zaror.cl", rol = rol)

    fun sesion(rol: String = "VECINO") = Sesion("jwt-123", 7, "Camila Rojas", "camila@zaror.cl", rol)

    fun usuario() = UsuarioDto(
        id = 7, nombre = "Camila Rojas", email = "camila@zaror.cl", telefono = "+56912345678",
        numeroCasa = "12", fechaNacimiento = "1990-04-12", recibirAvisos = true
    )

    fun feriado(fecha: LocalDate, nombre: String = "Feriado") = Feriado(fecha.toString(), nombre)
}
