package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.data.remote.dto.ActualizarUsuarioRequest
import com.example.appjuntavecinos.data.remote.dto.AuthResponse
import com.example.appjuntavecinos.data.remote.dto.CambiarEstadoRequest
import com.example.appjuntavecinos.data.remote.dto.CategoriaDto
import com.example.appjuntavecinos.data.remote.dto.CrearInformeRequest
import com.example.appjuntavecinos.data.remote.dto.CrearReservaRequest
import com.example.appjuntavecinos.data.remote.dto.InformeDto
import com.example.appjuntavecinos.data.remote.dto.LoginRequest
import com.example.appjuntavecinos.data.remote.dto.MovimientoDto
import com.example.appjuntavecinos.data.remote.dto.MovimientoRequest
import com.example.appjuntavecinos.data.remote.dto.OcupacionDto
import com.example.appjuntavecinos.data.remote.dto.PagoDto
import com.example.appjuntavecinos.data.remote.dto.RegisterRequest
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.dto.ResumenDto
import com.example.appjuntavecinos.data.remote.dto.SedeDto
import com.example.appjuntavecinos.data.remote.dto.UsuarioDto
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarRequest
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Todas las rutas del backend (contrato en API.md).
 * Las rutas van SIN "/" inicial porque BASE_URL ya termina en "/".
 * El token NO se pone aquí: lo agrega AuthInterceptor a cada petición.
 * Si el backend responde un error (4xx/5xx), Retrofit lanza HttpException.
 */
interface ApiService {

    // ------------------------------ AUTH (público) ------------------------------
    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun registrar(@Body datos: RegisterRequest): AuthResponse

    // -------------------------------- USUARIOS --------------------------------
    @GET("api/usuarios/me")
    suspend fun miPerfil(): UsuarioDto

    @PUT("api/usuarios/me")
    suspend fun actualizarPerfil(@Body datos: ActualizarUsuarioRequest): UsuarioDto

    // --------------------------------- SEDES ---------------------------------
    @GET("api/sedes")
    suspend fun sedes(): List<SedeDto>

    @GET("api/sedes/{id}/disponibilidad")
    suspend fun disponibilidad(
        @Path("id") sedeId: Long,
        @Query("anio") anio: Int,
        @Query("mes") mes: Int
    ): List<OcupacionDto>

    // -------------------------------- RESERVAS --------------------------------
    @POST("api/reservas")
    suspend fun crearReserva(@Body datos: CrearReservaRequest): ReservaDto

    @GET("api/reservas/mias")
    suspend fun misReservas(): List<ReservaDto>

    // estado = null -> todas (Retrofit omite los @Query nulos)
    @GET("api/reservas")
    suspend fun reservas(@Query("estado") estado: String? = null): List<ReservaDto>

    @PUT("api/reservas/{id}/estado")
    suspend fun cambiarEstadoReserva(@Path("id") id: Long, @Body datos: CambiarEstadoRequest): ReservaDto

    @DELETE("api/reservas/{id}")
    suspend fun cancelarReserva(@Path("id") id: Long): ReservaDto

    // -------------------------------- FINANZAS --------------------------------
    @GET("api/finanzas/resumen")
    suspend fun resumen(@Query("anio") anio: Int, @Query("mes") mes: Int): ResumenDto

    @GET("api/categorias")
    suspend fun categorias(): List<CategoriaDto>

    @GET("api/movimientos")
    suspend fun movimientos(@Query("anio") anio: Int, @Query("mes") mes: Int): List<MovimientoDto>

    // multipart: parte "datos" (JSON) + parte "archivo" opcional (JPG)
    @Multipart
    @POST("api/movimientos")
    suspend fun crearMovimiento(
        @Part("datos") datos: RequestBody,
        @Part archivo: MultipartBody.Part?
    ): MovimientoDto

    @PUT("api/movimientos/{id}")
    suspend fun editarMovimiento(@Path("id") id: Long, @Body datos: MovimientoRequest): MovimientoDto

    @DELETE("api/movimientos/{id}")
    suspend fun anularMovimiento(@Path("id") id: Long): MovimientoDto

    // GET api/movimientos/{id}/comprobante -> imagen. No va aquí: la carga Coil
    // con la URL de ApiConfig.urlComprobante(id), usando el mismo cliente con token.

    // ---------------------------------- PAGOS ----------------------------------
    @GET("api/pagos")
    suspend fun pagos(): List<PagoDto>

    // multipart: parte "datos" (JSON) + parte "archivo" OBLIGATORIA (JPG)
    @Multipart
    @POST("api/pagos")
    suspend fun registrarPago(
        @Part("datos") datos: RequestBody,
        @Part archivo: MultipartBody.Part
    ): PagoDto

    @POST("api/pagos/webpay/iniciar")
    suspend fun iniciarWebpay(@Body datos: WebpayIniciarRequest): WebpayIniciarResponse

    // -------------------------------- INFORMES --------------------------------
    @GET("api/informes")
    suspend fun informes(): List<InformeDto>

    @GET("api/informes/{id}")
    suspend fun informe(@Path("id") id: Long): InformeDto

    @POST("api/informes")
    suspend fun crearInforme(@Body datos: CrearInformeRequest): InformeDto
}
