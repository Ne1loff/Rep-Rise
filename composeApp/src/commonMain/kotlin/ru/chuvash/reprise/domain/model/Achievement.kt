package ru.chuvash.reprise.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

// Централизованный список всех достижений в приложении
object AchievementsList {
    val all = listOf(
        Achievement("first_workout", "Первый шаг", "Выполнить первую тренировку", Icons.Default.FitnessCenter),
        Achievement("streak_3", "Начало положено", "Удерживать стрик 3 дня", Icons.Default.LocalFireDepartment),
        Achievement("streak_7", "Стойкий", "Удерживать стрик 7 дней", Icons.Default.Whatshot),
        Achievement("total_reps_1000", "Тысячник", "Сделать 1000 повторений в сумме", Icons.Default.TrendingUp),
        Achievement("total_reps_5000", "Титан", "Сделать 5000 повторений в сумме", Icons.Default.WorkspacePremium)
    )
}