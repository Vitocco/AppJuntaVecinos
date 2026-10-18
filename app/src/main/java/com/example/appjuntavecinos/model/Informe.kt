package com.example.appjuntavecinos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "informes")
data class Informe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val fechaPublicacion: Long,
    val esRendicion: Boolean = false
)