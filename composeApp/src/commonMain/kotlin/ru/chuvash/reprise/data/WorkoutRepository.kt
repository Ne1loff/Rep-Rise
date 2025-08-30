@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.data

import Exercise
import ExerciseType
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

    init {
        initDefaultExercises();
    }

    // --- Функции для работы с подходами (Sets) ---

    private fun initDefaultExercises() {
        queries.transaction {
            queries.upsertExercise(ExerciseType.REPS_ONLY.name, 1.0, "Отжимания")
            queries.upsertExercise(ExerciseType.REPS_ONLY.name, 0.5, "Приседания")
            queries.upsertExercise(ExerciseType.REPS_ONLY.name, 1.5, "Подтягивания")
            queries.upsertExercise(ExerciseType.REPS_AND_WEIGHT.name, 0.05, "Жим лежа")
            queries.upsertExercise(ExerciseType.TIME.name, 0.2, "Планка")
            queries.upsertExercise(ExerciseType.TIME_AND_DISTANCE.name, 0.00005, "Бег") // TODO: Поправить
        }
    }

    fun getAllExercises(): List<Exercise> {
        return queries.getAllExercises().executeAsList().map {
            Exercise(
                id = it.id,
                name = it.name,
                type = ExerciseType.valueOf(it.type),
                pointsCoefficient = it.pointsCoefficient
            )
        }
    }

    fun getSetById(id: String): WorkoutSet? {
        return queries.getSetById(id.toLong()).executeAsOneOrNull()?.let {
            val instant = Instant.fromEpochSeconds(it.timestamp)
            WorkoutSet(
                id = it.id.toString(),
                exercise = Exercise(
                    id = it.exerciseId,
                    name = it.exerciseName,
                    type = ExerciseType.valueOf(it.exerciseType),
                    pointsCoefficient = it.pointsCoefficient
                ),
                timestamp = it.timestamp,
                dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault()),
                reps = it.reps?.toInt(),
                weight = it.weight,
                durationSeconds = it.durationSeconds?.toInt(),
                distanceMeters = it.distanceMeters?.toInt(),
                effortPoints = it.effortPoints.toInt()
            )
        }
    }

    fun addSet(
        date: LocalDate,
        exerciseId: Long,
        reps: Int?,
        weight: Double?,
        duration: Int?,
        distance: Int?,
        points: Int
    ) {
        val nowTime = System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        val targetLocalDateTime = date.atTime(nowTime)
        val timestamp = targetLocalDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds

        queries.insertSet(
            exerciseId = exerciseId,
            timestamp = timestamp,
            reps = reps?.toLong(),
            weight = weight,
            durationSeconds = duration?.toLong(),
            distanceMeters = distance?.toLong(),
            effortPoints = points.toLong()
        )
    }

    fun updateSet(
        setId: String,
        exerciseId: Long,
        reps: Int?,
        weight: Double?,
        duration: Int?,
        distance: Int?,
        points: Int
    ) {
        queries.updateSetById(
            exerciseId = exerciseId,
            reps = reps?.toLong(),
            weight = weight,
            durationSeconds = duration?.toLong(),
            distanceMeters = distance?.toLong(),
            effortPoints = points.toLong(),
            id = setId.toLong()
        )
    }


    fun getSetsForDate(date: LocalDate): List<WorkoutSet> {
        return queries.getSetsByDate(date.toString()).executeAsList().map { entity ->
            val timestamp = Instant.fromEpochSeconds(entity.timestamp)
            val dateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())

            WorkoutSet(
                id = entity.id.toString(),
                exercise = Exercise(
                    id = entity.exerciseId,
                    name = entity.exerciseName,
                    type = ExerciseType.valueOf(entity.exerciseType),
                    pointsCoefficient = entity.pointsCoefficient
                ),
                timestamp = entity.timestamp,
                dateTime = dateTime,
                reps = entity.reps?.toInt(),
                weight = entity.weight,
                durationSeconds = entity.durationSeconds?.toInt(),
                distanceMeters = entity.distanceMeters?.toInt(),
                effortPoints = entity.effortPoints.toInt()
            )
        }
    }

    // --- Функции для работы с целями (Goals) ---

    fun getGoalForDate(date: LocalDate): DailyGoal? {
        val entity = queries.getGoalByDate(date.toString()).executeAsOneOrNull()
        return if (entity != null) {
            DailyGoal(
                date = entity.date,
                targetPoints = entity.targetPoints.toInt(),
                completedPoints = entity.completedPoints.toInt()
            )
        } else {
            // Если цели на сегодня нет, можно создать дефолтную
            val newGoal = DailyGoal(date = date.toString(), targetPoints = 100)
            saveGoal(newGoal)
            newGoal
        }
    }

    fun saveGoal(goal: DailyGoal) {
        queries.upsertGoal(
            date = goal.date,
            targetPoints = goal.targetPoints.toLong(),
            completedPoints = goal.completedPoints.toLong()
        )
    }

    fun updateCompletedPointsForDate(date: LocalDate, totalPoints: Int) {
        queries.updateCompletedPoints(
            completedPoints = totalPoints.toLong(),
            date = date.toString()
        )
    }

    // Новая функция для получения данных за месяц
    fun getGoalsForMonth(yearMonth: String): List<DailyGoal> { // yearMonth в формате "YYYY-MM"
        return queries.getGoalsForMonth(yearMonth).executeAsList().map { entity ->
            DailyGoal(
                date = entity.date,
                targetPoints = entity.targetPoints.toInt(),
                completedPoints = entity.completedPoints.toInt()
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
            if (goal != null && goal.completedPoints >= goal.targetPoints) {
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

    fun getTotalEffortPoints(): Int {
        return queries.getTotalEffortPoints().executeAsOneOrNull()?.sum?.toInt() ?: 0
    }

    fun getWeeklyGoals(): Map<DayOfWeek, Int> {
        return queries.getWeeklyGoals().executeAsList().associate {
            DayOfWeek(it.dayOfWeek.toInt()) to it.targetPoints.toInt()
        }
    }

    fun saveWeeklyGoal(day: DayOfWeek, target: Int) {
        queries.upsertWeeklyGoal(
            dayOfWeek = day.isoDayNumber.toLong(),
            targetPoints = target.toLong()
        )
    }

    fun getWeeklySummary(): List<WeeklySummary> {
        return queries.getWeeklySummary().executeAsList().map {
            WeeklySummary(weekId = it.weekId, totalReps = it.totalPoints?.toInt() ?: 0)
        }
    }

    fun getDailySummaryForLast30Days(): List<DailySummary> {
        return queries.getDailySummaryForLast30Days().executeAsList().map {
            DailySummary(dayId = it.dayId, totalReps = it.totalPoints?.toInt() ?: 0)
        }
    }

    fun getActiveDaysCount(): Int {
        return queries.getActiveDaysCount().executeAsOneOrNull()?.toInt() ?: 0
    }

}

