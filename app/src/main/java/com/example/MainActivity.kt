package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.ThemePreference
import com.example.ui.AddEditHabitScreen
import com.example.ui.HabitDetailsScreen
import com.example.ui.HabitViewModel
import com.example.ui.HomeScreen
import com.example.ui.Screen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by viewModel.themePreference.collectAsState()
            val isDarkTheme = when (themePreference) {
                ThemePreference.DARK -> true
                ThemePreference.LIGHT -> false
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home
                    ) {
                        composable<Screen.Home> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAddHabit = {
                                    navController.navigate(Screen.AddEditHabit())
                                },
                                onNavigateToHabitDetails = { habitId ->
                                    navController.navigate(Screen.HabitDetails(habitId))
                                }
                            )
                        }
                        
                        composable<Screen.HabitDetails> { backStackEntry ->
                            val details: Screen.HabitDetails = backStackEntry.toRoute()
                            HabitDetailsScreen(
                                viewModel = viewModel,
                                habitId = details.habitId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEdit = { habitId ->
                                    navController.navigate(Screen.AddEditHabit(habitId))
                                }
                            )
                        }
                        
                        composable<Screen.AddEditHabit> { backStackEntry ->
                            val addEdit: Screen.AddEditHabit = backStackEntry.toRoute()
                            AddEditHabitScreen(
                                viewModel = viewModel,
                                habitId = addEdit.habitId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
