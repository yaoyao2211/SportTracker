package com.example.sporttracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun getAll(): Flow<List<Workout>>

    @Insert
    suspend fun insert(workout: Workout): Long

    @Delete
    suspend fun delete(workout: Workout)
}