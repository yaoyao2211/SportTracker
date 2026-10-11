package com.example.sporttracker.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao, private val trackPointDao: TrackPointDao) {
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAll()

    suspend fun insertWorkout(workout: Workout): Long = workoutDao.insert(workout)
    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)
    suspend fun insertTrackPoints(points: List<TrackPoint>) = trackPointDao.insertAll(points)
}