enum class ExerciseType {
    REPS_ONLY,      // Только повторения (Отжимания)
    REPS_AND_WEIGHT,// Повторения и вес (Жим лежа)
    TIME,           // Только время (Планка)
    TIME_AND_DISTANCE // ИСПРАВЛЕНИЕ: Добавлен новый тип
}

data class Exercise(
    val id: Long,
    val name: String,
    val type: ExerciseType,
    val pointsCoefficient: Double // Коэффициент для расчета очков
)