package com.example.appjuntavecinos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appjuntavecinos.data.model.Informe
import com.example.appjuntavecinos.data.model.Movimiento
import com.example.appjuntavecinos.data.model.Reserva
import com.example.appjuntavecinos.data.model.Usuario

@Database(
    entities = [Reserva::class, Movimiento::class, Informe::class, Usuario::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservaDao(): ReservaDao
    abstract fun finanzasDao(): FinanzasDao
    abstract fun usuarioDao(): UsuarioDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "junta_database"
                ).fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
    }
}