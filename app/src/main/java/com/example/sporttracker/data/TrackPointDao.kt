package com.example.sporttracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackPointDao {
    @Insert
    suspend fun insertAll(points: List<TrackPoint>)

    @Query("SELECT * FROM track_points WHERE workoutId = :workoutId ORDER BY timestamp ASC")
    suspend fun getPointsForWorkout(workoutId: Long): List<TrackPoint>
}