@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.data.model


import Exercise
import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Представляет один подход (сет) в тренировке.
 * @param id Уникальный идентификатор.
 * @param reps Количество повторений.
 * @param timestamp Время выполнения.
 * @param exerciseType Тип упражнения.
 */
data class WorkoutSet(
    val id: String,
    val exercise: Exercise,
    val timestamp: Long,
    val dateTime: LocalDateTime,
    val reps: Int?,
    val weight: Double?,
    val durationSeconds: Int?,
    val distanceMeters: Int?,
    val effortPoints: Int
)

/**
 * Представляет дневную цель.
 * @param date Дата.
 * @param targetPoints Целевое количество очков.
 * @param completedPoints Выполненное количество очков.
 */
data class DailyGoal(
    val date: String,
    val targetPoints: Int, // Теперь цель в очках
    val completedPoints: Int = 0
)
