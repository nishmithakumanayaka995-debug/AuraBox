package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aura_snapshots")
data class AuraSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ecosystemType: String,
    val pillarHeight: Float,
    val auraIntensity: Float,
    val climateIndex: Float,
    val rotationAngle: Float,
    val timestamp: Long = System.currentTimeMillis()
)
