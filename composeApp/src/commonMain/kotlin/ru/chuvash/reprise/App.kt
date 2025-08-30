package ru.chuvash.reprise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import ru.chuvash.reprise.ui.AchievementsScreen
import ru.chuvash.reprise.ui.AddEditSetScreen
import ru.chuvash.reprise.ui.DashboardScreen
import ru.chuvash.reprise.ui.HistoryScreen
import ru.chuvash.reprise.ui.SettingsScreen
import ru.chuvash.reprise.ui.StatisticsScreen
import ru.chuvash.reprise.ui.theme.AppTheme

sealed class Screen {
    data class Dashboard(val initialDate: LocalDate? = null) : Screen()
    data class AddEditSet(val date: LocalDate, val setId: String? = null) : Screen()
    object Settings : Screen()
    object History : Screen()
    object Achievements : Screen()
    object Statistics : Screen()
}

@Composable
fun App() {
    AppTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard()) }

        // 3. Используем when для выбора, что показать
        when (val screen = currentScreen) {
            is Screen.Dashboard -> {
                DashboardScreen(
                    initialDate = screen.initialDate,
                    // 4. Передаем функцию для навигации на другой экран
                    onNavigateToSettings = {
                        currentScreen = Screen.Settings
                    },
                    onNavigateToHistory = {
                        currentScreen = Screen.History
                    },
                    onNavigateToAchievements = {
                        currentScreen = Screen.Achievements
                    },
                    onNavigateToStatistics = {
                        currentScreen = Screen.Statistics
                    },
                    onNavigateToAddSet = {
                        currentScreen = Screen.AddEditSet(date = it)
                    }
                )
            }

            is Screen.AddEditSet -> AddEditSetScreen(onNavigateBack = {
                currentScreen = Screen.Dashboard(initialDate = screen.date)
            })

            is Screen.Settings -> {
                SettingsScreen( // Это будет наш новый экран
                    onNavigateBack = {
                        currentScreen = Screen.Dashboard()
                    }
                )
            }

            is Screen.History -> {
                HistoryScreen( // Это будет наш новый экран
                    onNavigateBack = {
                        currentScreen = Screen.Dashboard()
                    },
                    onDateLongPress = { date -> currentScreen = Screen.Dashboard(initialDate = date) }
                )
            }

            is Screen.Achievements -> AchievementsScreen(onNavigateBack = {
                currentScreen = Screen.Dashboard()
            })

            is Screen.Statistics -> StatisticsScreen(onNavigateBack = {
                currentScreen = Screen.Dashboard()
            })
        }
    }
}