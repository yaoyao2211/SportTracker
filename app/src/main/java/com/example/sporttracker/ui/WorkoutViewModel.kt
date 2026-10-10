package com.example.sporttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sporttracker.data.AppDatabase
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
        val dao = AppDatabase.getDatabase(application).workoutDao()
        repository = WorkoutRepository(dao)
        workouts = repository.allWorkouts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun saveWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.insert(workout)
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.delete(workout)
        }
    }
}