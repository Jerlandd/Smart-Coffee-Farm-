package com.example.smartcoffeefarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "coffee_plots")
data class CoffeePlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val name: String,
    val variety: String,
    val areaHa: Double = 1.0,
    val address: String = "",
    val latitude: Double = -6.2000,
    val longitude: Double = 106.8166,
    val plantingDate: String,
    val soilMoisture: String,
    val healthStatus: String,
    val notes: String,
    val imageUri: String? = null
) : Serializable
