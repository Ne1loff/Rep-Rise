package ru.chuvash.reprise.domain.services


import Exercise
import kotlin.math.roundToInt

class WorkoutService {
    fun calculateEffortPoints(
        exercise: Exercise,
        reps: Int?,
        weight: Double?,
        durationSeconds: Int?,
        distanceMeters: Int? // ИСПРАВЛЕНИЕ: Новый параметр
    ): Int {
        val points = when (exercise.type) {
            ExerciseType.REPS_ONLY -> (reps ?: 0) * exercise.pointsCoefficient
            ExerciseType.REPS_AND_WEIGHT -> (reps ?: 0) * (weight ?: 1.0) * exercise.pointsCoefficient
            ExerciseType.TIME -> (durationSeconds ?: 0) * exercise.pointsCoefficient
            ExerciseType.TIME_AND_DISTANCE -> (distanceMeters ?: 0) * (durationSeconds ?: 0) * exercise.pointsCoefficient
        }
        return points.roundToInt()
    }
}