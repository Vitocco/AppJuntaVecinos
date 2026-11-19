package com.example.appjuntavecinos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room se usa SOLO como caché del backend: sedes y "mis reservas".
// Versión 7: se quitaron las tablas de las pantallas antiguas (reservas, movimientos,
// informes locales). La fuente oficial de todos los datos es el backend.
@Database(
    entities = [SedeEntity::class, ReservaEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sedeDao(): SedeDao
    abstract fun reservaCacheDao(): ReservaCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "junta_database"
                )
                    // Desarrollo: al subir la versión se borra y recrea la base.
                    // No se pierde nada importante: es solo caché del backend.
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
    }
}
