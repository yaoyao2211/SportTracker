package com.example.sporttracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sporttracker.data.SportType
import com.example.sporttracker.data.Workout
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val startDateStr = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(startTime)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sport.displayName) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            // 显示开始日期
            Text(
                text = "开始时间：$startDateStr",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 大号计时器
            Text(
                text = formatDuration(elapsedMillis),
                fontSize = 72.sp,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (sport.isStrength) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("已完成组数", style = MaterialTheme.typography.labelLarge)
                        Text("$sets", fontSize = 36.sp, style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { sets++ }, shape = RoundedCornerShape(8.dp)) {
                            Text("+1 组")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 操作按钮
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { isRunning = !isRunning },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(if (isRunning) "暂停" else "开始", fontSize = 18.sp)
                }
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        elapsedMillis = 0
                        sets = 0
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("重置", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
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
                enabled = elapsedMillis > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("保存记录", fontSize = 18.sp)
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