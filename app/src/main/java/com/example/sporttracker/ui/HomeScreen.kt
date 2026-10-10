package com.example.sporttracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sporttracker.data.SportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSportSelected: (SportType) -> Unit,
    onHistoryClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("运动记录") },
                actions = {
                    TextButton(onClick = onHistoryClick) {
                        Text("历史")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(SportType.values()) { sport ->
                Card(
                    onClick = { onSportSelected(sport) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sport.displayName,
                            fontSize = 22.sp,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}