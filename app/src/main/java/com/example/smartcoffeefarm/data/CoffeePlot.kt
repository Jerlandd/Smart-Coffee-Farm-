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
    val plantingDate: String,
    val soilMoisture: String,
    val healthStatus: String,
    val notes: String,
    val imageUri: String? = null
) : Serializable
