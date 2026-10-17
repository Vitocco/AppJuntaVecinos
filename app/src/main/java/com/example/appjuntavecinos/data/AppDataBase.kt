package com.example.appjuntavecinos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appjuntavecinos.model.Reserva
import com.example.appjuntavecinos.model.Movimiento
import com.example.appjuntavecinos.model.Informe

@Database(
    entities = [Reserva::class, Movimiento::class, Informe::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservaDao(): ReservaDao
    abstract fun finanzasDao(): FinanzasDao


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