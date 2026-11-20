package com.example.smartcoffeefarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "harvest_records")
data class HarvestRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val date: String,
    val blockName: String,
    val quantityKg: Int,
    val quality: String,
    val sellingPriceRp: Double = 0.0
) : Serializable
