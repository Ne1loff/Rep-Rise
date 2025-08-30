@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ru.chuvash.reprise.cache.AppDatabase
import ru.chuvash.reprise.data.model.DailyGoal
import ru.chuvash.reprise.data.model.DailySummary
import ru.chuvash.reprise.data.model.WeeklySummary
import ru.chuvash.reprise.data.model.WorkoutSet
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class WorkoutRepository(database: AppDatabase) {
    private val queries = database.workoutQueries

    // --- Функции для работы с подходами (Sets) ---

    fun addWorkoutSet(reps: Int, exerciseType: String, date: LocalDate) {
        // 1. Получаем текущее время.
        val nowTime = System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        // 2. Создаем LocalDateTime, комбинируя переданную ДАТУ и текущее ВРЕМЯ.
        val targetLocalDateTime = date.atTime(nowTime)
        // 3. Конвертируем в универсальный Instant (UTC), а затем в Unix-timestamp для сохранения.
        val timestamp = targetLocalDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds

        queries.insertSet(
            reps = reps.toLong(),
            timestamp = timestamp,
            exerciseType = exerciseType
        )
    }

    fun getSetsForDate(date: LocalDate): List<WorkoutSet> {
        return queries.getSetsByDate(date.toString()).executeAsList().map { entity ->
            val timestamp = Instant.fromEpochSeconds(entity.timestamp)
            val dateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())

            WorkoutSet(
                id = entity.id.toString(),
                reps = entity.reps.toInt(),
                timestamp = timestamp,
                exerciseType = entity.exerciseType,
                dateTime = dateTime
            )
        }
    }

    // --- Функции для работы с целями (Goals) ---

    fun getGoalForDate(date: LocalDate): DailyGoal? {
        val entity = queries.getGoalByDate(date.toString()).executeAsOneOrNull()
        return if (entity != null) {
            DailyGoal(
                date = entity.date,
                targetReps = entity.targetRps.toInt(),
                completedReps = entity.completedReps.toInt()
            )
        } else {
            // Если цели на сегодня нет, можно создать дефолтную
            val newGoal = DailyGoal(date = date.toString(), targetReps = 100)
            saveGoal(newGoal)
            newGoal
        }
    }

    fun saveGoal(goal: DailyGoal) {
        queries.upsertGoal(
            date = goal.date,
            targetRps = goal.targetReps.toLong(),
            completedReps = goal.completedReps.toLong()
        )
    }

    fun updateCompletedRepsForDate(date: LocalDate, totalReps: Int) {
        queries.updateCompletedReps(completedReps = totalReps.toLong(), date = date.toString())
    }

    // Новая функция для получения данных за месяц
    fun getGoalsForMonth(yearMonth: String): List<DailyGoal> { // yearMonth в формате "YYYY-MM"
        return queries.getGoalsForMonth(yearMonth).executeAsList().map { entity ->
            DailyGoal(
                date = entity.date,
                targetReps = entity.targetRps.toInt(),
                completedReps = entity.completedReps.toInt()
            )
        }
    }

    // Новая функция для удаления подхода
    fun deleteWorkoutSetById(id: String) {
        // ID приходит как String, но в БД он INTEGER
        queries.deleteSetById(id.toLong())
    }

    // Новая функция для подсчета стрика
    fun calculateCurrentStreak(): Int {
        var streak = 0
        var currentDate = System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        while (true) {
            val goal = queries.getGoalByDate(currentDate.toString()).executeAsOneOrNull()
            if (goal != null && goal.completedReps >= goal.targetRps) {
                streak++
                currentDate = currentDate.minus(1, DateTimeUnit.DAY)
            } else {
                // Если сегодня цель еще не выполнена, проверяем стрик до вчерашнего дня
                if (streak == 0 && currentDate == System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                ) {
                    currentDate = currentDate.minus(1, DateTimeUnit.DAY)
                    continue // Пробуем еще раз, но уже со вчерашнего дня
                }
                break // Прерываем цикл, если день пропущен или цели не было
            }
        }
        return streak
    }

    fun getUnlockedAchievementIds(): Set<String> {
        return queries.getUnlockedAchievementIds().executeAsList().toSet()
    }

    fun unlockAchievement(id: String) {
        queries.unlockAchievement(id, System.now().epochSeconds)
    }

    fun getTotalReps(): Int {
        return queries.getTotalReps().executeAsOneOrNull()?.sum?.toInt() ?: 0
    }

    fun getWeeklyGoals(): Map<DayOfWeek, Int> {
        return queries.getWeeklyGoals().executeAsList().associate {
            DayOfWeek(it.dayOfWeek.toInt()) to it.targetReps.toInt()
        }
    }

    fun saveWeeklyGoal(day: DayOfWeek, target: Int) {
        queries.upsertWeeklyGoal(
            dayOfWeek = day.isoDayNumber.toLong(),
            targetReps = target.toLong()
        )
    }

    fun getWeeklySummary(): List<WeeklySummary> {
        return queries.getWeeklySummary().executeAsList().map {
            WeeklySummary(weekId = it.weekId, totalReps = it.totalReps?.toInt() ?: 0)
        }
    }

    fun getDailySummaryForLast30Days(): List<DailySummary> {
        return queries.getDailySummaryForLast30Days().executeAsList().map {
            DailySummary(dayId = it.dayId, totalReps = it.totalReps?.toInt() ?: 0)
        }
    }

    fun getActiveDaysCount(): Int {
        return queries.getActiveDaysCount().executeAsOneOrNull()?.toInt() ?: 0
    }

    fun updateWorkoutSet(id: String, reps: Int, exerciseType: String) {
        queries.updateSetById(reps = reps.toLong(), exerciseType = exerciseType, id = id.toLong())
    }
}

