package com.example.sporttracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sporttracker.data.SportType
import com.example.sporttracker.data.Workout
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    sport: SportType,
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    var elapsedMillis by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var sets by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedMillis += 1000L
        }
    }

    val startTime = remember { System.currentTimeMillis() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sport.displayName) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatDuration(elapsedMillis),
                fontSize = 64.sp,
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (sport.isStrength) {
                Text("已完成组数: $sets", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { sets++ }) {
                    Text("完成一组")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { isRunning = !isRunning }) {
                    Text(if (isRunning) "暂停" else "开始")
                }
                OutlinedButton(onClick = {
                    isRunning = false
                    elapsedMillis = 0
                    sets = 0
                }) {
                    Text("重置")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val workout = Workout(
                        type = sport.name,
                        startTime = startTime,
                        endTime = System.currentTimeMillis(),
                        totalDurationMillis = elapsedMillis,
                        note = if (sport.isStrength) "组数: $sets" else null
                    )
                    viewModel.saveWorkout(workout)
                    onBack()
                },
                enabled = elapsedMillis > 0
            ) {
                Text("保存记录")
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}