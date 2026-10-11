package com.example.sporttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_points")
data class TrackPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)