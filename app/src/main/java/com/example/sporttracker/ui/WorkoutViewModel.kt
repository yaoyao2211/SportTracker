package com.example.sporttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sporttracker.data.AppDatabase
import com.example.sporttracker.data.TrackPoint
import com.example.sporttracker.data.Workout
import com.example.sporttracker.data.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository
    val workouts: StateFlow<List<Workout>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WorkoutRepository(db.workoutDao(), db.trackPointDao())
        workouts = repository.allWorkouts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun saveWorkout(workout: Workout, points: List<TrackPoint>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val workoutId = repository.insertWorkout(workout)
            // 将内存中的轨迹点绑定上 workoutId 并保存到数据库
            val finalPoints = points.map { it.copy(workoutId = workoutId) }
            repository.insertTrackPoints(finalPoints)
            onComplete()
        }
    }
}