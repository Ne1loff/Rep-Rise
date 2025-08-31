package ru.chuvash.reprise.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import reprise.composeapp.generated.resources.Res
import reprise.composeapp.generated.resources.achieve_curious_description
import reprise.composeapp.generated.resources.achieve_curious_title
import reprise.composeapp.generated.resources.achieve_early_bird_description
import reprise.composeapp.generated.resources.achieve_early_bird_title
import reprise.composeapp.generated.resources.achieve_first_workout_description
import reprise.composeapp.generated.resources.achieve_first_workout_title
import reprise.composeapp.generated.resources.achieve_marathon_runner_description
import reprise.composeapp.generated.resources.achieve_marathon_runner_title
import reprise.composeapp.generated.resources.achieve_night_owl_description
import reprise.composeapp.generated.resources.achieve_night_owl_title
import reprise.composeapp.generated.resources.achieve_rest_day_champion_description
import reprise.composeapp.generated.resources.achieve_rest_day_champion_title
import reprise.composeapp.generated.resources.achieve_streak_30_description
import reprise.composeapp.generated.resources.achieve_streak_30_title
import reprise.composeapp.generated.resources.achieve_streak_3_description
import reprise.composeapp.generated.resources.achieve_streak_3_title
import reprise.composeapp.generated.resources.achieve_streak_7_description
import reprise.composeapp.generated.resources.achieve_streak_7_title
import reprise.composeapp.generated.resources.achieve_time_traveler_description
import reprise.composeapp.generated.resources.achieve_time_traveler_title
import reprise.composeapp.generated.resources.achieve_total_points_1000_description
import reprise.composeapp.generated.resources.achieve_total_points_1000_title
import reprise.composeapp.generated.resources.achieve_total_points_5000_description
import reprise.composeapp.generated.resources.achieve_total_points_5000_title
import reprise.composeapp.generated.resources.achieve_total_weight_10000_description
import reprise.composeapp.generated.resources.achieve_total_weight_10000_title
import reprise.composeapp.generated.resources.achieve_weekend_warrior_description
import reprise.composeapp.generated.resources.achieve_weekend_warrior_title

data class Achievement(
    val id: String,
    val title: StringResource,
    val description: StringResource,
    val icon: ImageVector,
    val isSecret: Boolean = false
)

// Централизованный список всех достижений в приложении
object AchievementsList {
    val all = listOf(
        Achievement(
            "first_workout",
            Res.string.achieve_first_workout_title,
            Res.string.achieve_first_workout_description,
            Icons.Default.FitnessCenter
        ),
        Achievement(
            "streak_3",
            Res.string.achieve_streak_3_title,
            Res.string.achieve_streak_3_description,
            Icons.Default.LocalFireDepartment
        ),
        Achievement(
            "streak_7",
            Res.string.achieve_streak_7_title,
            Res.string.achieve_streak_7_description,
            Icons.Default.Whatshot
        ),
        Achievement(
            "total_points_1000",
            Res.string.achieve_total_points_1000_title,
            Res.string.achieve_total_points_1000_description,
            Icons.Default.TrendingUp
        ),
        Achievement(
            "total_points_5000",
            Res.string.achieve_total_points_5000_title,
            Res.string.achieve_total_points_5000_description,
            Icons.Default.WorkspacePremium
        ),
        Achievement(
            "streak_30",
            Res.string.achieve_streak_30_title,
            Res.string.achieve_streak_30_description,
            Icons.Default.MilitaryTech
        ),
        Achievement(
            "total_weight_10000",
            Res.string.achieve_total_weight_10000_title,
            Res.string.achieve_total_weight_10000_description,
            Icons.Default.Scale
        ),
        Achievement(
            "marathon_runner",
            Res.string.achieve_marathon_runner_title,
            Res.string.achieve_marathon_runner_description,
            Icons.Default.DirectionsRun
        ),
        Achievement(
            "early_bird",
            Res.string.achieve_early_bird_title,
            Res.string.achieve_early_bird_description,
            Icons.Default.WbSunny
        ),
        Achievement(
            "night_owl",
            Res.string.achieve_night_owl_title,
            Res.string.achieve_night_owl_description,
            Icons.Default.NightsStay
        ),
        Achievement(
            "weekend_warrior",
            Res.string.achieve_weekend_warrior_title,
            Res.string.achieve_weekend_warrior_description,
            Icons.Default.CalendarViewWeek
        ),

        // Секретные
        Achievement(
            "rest_day_champion",
            Res.string.achieve_rest_day_champion_title,
            Res.string.achieve_rest_day_champion_description,
            Icons.Default.SelfImprovement,
            isSecret = true
        ),
        Achievement(
            "time_traveler",
            Res.string.achieve_time_traveler_title,
            Res.string.achieve_time_traveler_description,
            Icons.Default.History,
            isSecret = true
        ),
        Achievement(
            "curious",
            Res.string.achieve_curious_title,
            Res.string.achieve_curious_description,
            Icons.Default.Explore,
            isSecret = true
        )
    )
}