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
import ru.chuvash.reprise.data.model.DailyGoal
import ru.chuvash.reprise.domain.model.Achievement
import ru.chuvash.reprise.domain.model.AchievementsList
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class AchievementService(private val repository: WorkoutRepository) {

    private val visitedScreens = mutableSetOf<String>()

    fun notifyScreenVisited(screenName: String) {
        visitedScreens.add(screenName)
    }

    // ИСПРАВЛЕНИЕ 1: Переносим логику расчета стрика сюда и оптимизируем ее
    fun calculateCurrentStreak(today: LocalDate, allRecentGoals: List<DailyGoal>): Int {
        var streak = 0
        var currentDate = today
        val goalsMap = allRecentGoals.associateBy { LocalDate.parse(it.date) }

        // Проверяем стрик, начиная с сегодняшнего дня
        while (true) {
            val goal = goalsMap[currentDate]
            if (goal != null && goal.completedPoints >= goal.targetPoints) {
                streak++
                currentDate = currentDate.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }

        // Если сегодня цель еще не выполнена, возможно, стрик закончился вчера
        val currentDayGoal = goalsMap[today]
        if (currentDayGoal == null || currentDayGoal.completedPoints < currentDayGoal.targetPoints) {
            var yesterdayStreak = 0
            var yesterday = today.minus(1, DateTimeUnit.DAY)
            while(true) {
                val goal = goalsMap[yesterday]
                if (goal != null && goal.completedPoints >= goal.targetPoints) {
                    yesterdayStreak++
                    yesterday = yesterday.minus(1, DateTimeUnit.DAY)
                } else {
                    break
                }
            }
            return yesterdayStreak
        }

        return streak
    }

    fun checkAndUnlockAchievements(
        currentDate: LocalDate,
        streak: Int,
        totalPoints: Int
    ): List<Achievement> {
        val unlockedIds = repository.getUnlockedAchievementIds()
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