package com.example.appjuntavecinos.data.remote.dto

// --------------------------- FINANZAS ---------------------------

// GET api/finanzas/resumen?anio=&mes=   (saldo = acumulado al cierre del mes)
data class ResumenDto(
    val saldo: Long,
    val ingresosMes: Long,
    val gastosMes: Long,
    val gastosPorCategoria: List<GastoCategoriaDto>?
)

data class GastoCategoriaDto(
    val categoria: String,
    val total: Long
)

// GET api/categorias
data class CategoriaDto(
    val id: Long,
    val nombre: String,
    val tipo: String         // INGRESO | GASTO
)

// Objeto movimiento
data class MovimientoDto(
    val id: Long,
    val tipo: String,                // INGRESO | GASTO
    val categoriaId: Long,
    val categoriaNombre: String?,
    val reservaId: Long?,
    val descripcion: String?,
    val monto: Long,
    val fechaMovimiento: String,
    val estado: String,              // VIGENTE | ANULADO
    val usuarioRegistroId: Long?,
    val tieneComprobante: Boolean
)

// Parte "datos" de POST api/movimientos (multipart) y body de PUT api/movimientos/{id}
data class MovimientoRequest(
    val tipo: String,
    val categoriaId: Long,
    val descripcion: String,
    val monto: Long,
    val fechaMovimiento: String
)

// ----------------------------- PAGOS -----------------------------

// Objeto pago
data class PagoDto(
    val id: Long,
    val reservaId: Long,
    val usuarioNombre: String?,
    val sedeNombre: String?,
    val fechaReserva: String?,
    val monto: Long,
    val fechaPago: String?,
    val medio: String,               // WEBPAY | TRANSFERENCIA | EFECTIVO | OTRO
    val referencia: String?,
    val estado: String,              // PENDIENTE | PAGADO | RECHAZADO
    val movimientoId: Long?
)

// Parte "datos" de POST api/pagos (multipart; el archivo es obligatorio)
data class PagoRequest(
    val reservaId: Long,
    val monto: Long,
    val medio: String,               // TRANSFERENCIA | EFECTIVO | OTRO  (WEBPAY no se permite aquí)
    val fechaPago: String,
    val referencia: String
)

// POST api/pagos/webpay/iniciar   (Etapa 4)
data class WebpayIniciarRequest(val reservaId: Long)
data class WebpayIniciarResponse(val url: String, val token: String)

// ---------------------------- INFORMES ----------------------------

// GET api/informes (lista) y GET api/informes/{id} (detalle con totales).
// Un solo DTO: en la lista los totales llegan vacíos (null).
data class InformeDto(
    val id: Long,
    val titulo: String,
    val periodoInicio: String,
    val periodoFin: String,
    val observacion: String?,
    val publicado: Boolean,
    val fechaGeneracion: String?,    // "2026-11-03T20:00:00"
    val totalIngresos: Long?,
    val totalGastos: Long?,
    val saldoPeriodo: Long?,
    val gastosPorCategoria: List<GastoCategoriaDto>?
)

// POST api/informes
data class CrearInformeRequest(
    val titulo: String,
    val periodoInicio: String,
    val periodoFin: String,
    val observacion: String,
    val publicado: Boolean
)
