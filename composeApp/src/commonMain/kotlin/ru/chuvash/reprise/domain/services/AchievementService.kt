package ru.chuvash.reprise.domain.services

import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.domain.model.Achievement
import ru.chuvash.reprise.domain.model.AchievementsList


class AchievementService(private val repository: WorkoutRepository) {

    fun checkAndUnlockAchievements(): List<Achievement> {
        val unlockedIds = repository.getUnlockedAchievementIds()
        val streak = repository.calculateCurrentStreak()
        val totalReps = repository.getTotalEffortPoints()
        val newlyUnlocked = mutableListOf<Achievement>()

        AchievementsList.all.forEach { achievement ->
            if (achievement.id !in unlockedIds) {
                val shouldUnlock = when (achievement.id) {
                    "first_workout" -> totalReps > 0
                    "streak_3" -> streak >= 3
                    "streak_7" -> streak >= 7
                    "total_reps_1000" -> totalReps >= 1000
                    "total_reps_5000" -> totalReps >= 5000
                    else -> false
                }
                if (shouldUnlock) {
                    repository.unlockAchievement(achievement.id)
                    newlyUnlocked.add(achievement) // Добавляем в список для показа
                }
            }
        }
        return newlyUnlocked
    }
}