package com.example.sporttracker.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutDao) {
    val allWorkouts: Flow<List<Workout>> = dao.getAll()

    suspend fun insert(workout: Workout) = dao.insert(workout)
    suspend fun delete(workout: Workout) = dao.delete(workout)
}