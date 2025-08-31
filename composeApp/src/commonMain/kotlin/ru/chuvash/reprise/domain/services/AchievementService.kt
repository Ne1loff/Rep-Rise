@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.domain.services

import ExerciseNameKey
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.domain.model.Achievement
import ru.chuvash.reprise.domain.model.AchievementsList
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class AchievementService(private val repository: WorkoutRepository) {

    private val visitedScreens = mutableSetOf<String>()

    fun notifyScreenVisited(screenName: String) {
        visitedScreens.add(screenName)
    }

    fun checkAndUnlockAchievements(currentDate: LocalDate): List<Achievement> {
        val unlockedIds = repository.getUnlockedAchievementIds()
        val totalPoints = repository.getTotalEffortPoints()
        val streak = repository.calculateCurrentStreak()
        val newlyUnlocked = mutableListOf<Achievement>()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        AchievementsList.all.forEach { achievement ->
            if (achievement.id !in unlockedIds) {
                val shouldUnlock = when (achievement.id) {
                    "first_workout" -> totalPoints > 0
                    "streak_3" -> streak >= 3
                    "streak_7" -> streak >= 7
                    "streak_30" -> streak >= 30
                    "total_points_1000" -> totalPoints >= 1000
                    "total_points_5000" -> totalPoints >= 5000
                    "total_weight_10000" -> repository.getTotalWeightLifted() >= 10000
                    "marathon_runner" -> repository.getTotalDistanceForExercise(ExerciseNameKey.RUNNING.value) >= 42195
                    "early_bird" -> repository.getSetCountBetweenHours(0, 7) > 0
                    "night_owl" -> repository.getSetCountBetweenHours(22, 24) > 0
                    "weekend_warrior" -> {
                        val dayOfWeek = today.dayOfWeek.isoDayNumber
                        val recentSunday = today.minus(dayOfWeek % 7, DateTimeUnit.DAY)
                        val recentSaturday = recentSunday.minus(1, DateTimeUnit.DAY)
                        repository.getWorkoutCountOnDate(recentSaturday) > 0 && repository.getWorkoutCountOnDate(
                            recentSunday
                        ) > 0
                    }

                    "rest_day_champion" -> {
                        val goalForToday = repository.getGoalForDate(today)
                        val setsToday = repository.getSetsForDate(today)
                        goalForToday != null && goalForToday.targetPoints == 0 && setsToday.isEmpty()
                    }

                    "time_traveler" -> currentDate < today.minus(1, DateTimeUnit.YEAR)
                    "curious" -> visitedScreens.containsAll(
                        listOf(
                            "History",
                            "Statistics",
                            "Achievements",
                            "Settings"
                        )
                    )

                    else -> false
                }
                if (shouldUnlock) {
                    repository.unlockAchievement(achievement.id)
                    newlyUnlocked.add(achievement)
                }
            }
        }
        return newlyUnlocked.distinctBy { it.id }
    }
}