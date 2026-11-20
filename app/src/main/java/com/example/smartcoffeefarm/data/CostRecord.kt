package com.example.smartcoffeefarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "cost_records")
data class CostRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val category: String,
    val title: String,
    val amountRp: Double,
    val date: String
) : Serializable
