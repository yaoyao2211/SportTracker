package com.example.sporttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sporttracker.data.SportType
import com.example.sporttracker.ui.HistoryScreen
import com.example.sporttracker.ui.HomeScreen
import com.example.sporttracker.ui.RecordScreen
import com.example.sporttracker.ui.theme.SportTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportTrackerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onSportSelected = { sport ->
                                    navController.navigate("record/${sport.name}")
                                },
                                onHistoryClick = { navController.navigate("history") }
                            )
                        }
                        composable("record/{sport}") { backStackEntry ->
                            val sportName = backStackEntry.arguments?.getString("sport")
                                ?: SportType.RUNNING.name
                            val sport = SportType.valueOf(sportName)
                            RecordScreen(sport = sport, onBack = { navController.popBackStack() })
                        }
                        composable("history") {
                            HistoryScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}