import org.jetbrains.compose.resources.StringResource
import reprise.composeapp.generated.resources.Res
import reprise.composeapp.generated.resources.exercise_bench_press
import reprise.composeapp.generated.resources.exercise_plank
import reprise.composeapp.generated.resources.exercise_pullups
import reprise.composeapp.generated.resources.exercise_pushups
import reprise.composeapp.generated.resources.exercise_running
import reprise.composeapp.generated.resources.exercise_squats

enum class ExerciseType {
    REPS_ONLY,      // Только повторения (Отжимания)
    REPS_AND_WEIGHT,// Повторения и вес (Жим лежа)
    TIME,           // Только время (Планка)
    TIME_AND_DISTANCE // ИСПРАВЛЕНИЕ: Добавлен новый тип
}

enum class ExerciseNameKey(val value: String, val resource: StringResource) {
    PUSH_UPS("pushups", Res.string.exercise_pushups),
    SQUATS("squats", Res.string.exercise_squats),
    PULL_UPS("pullups", Res.string.exercise_pullups),
    BENCH_PRESS("bench_press", Res.string.exercise_bench_press),
    PLANK("plank", Res.string.exercise_plank),
    RUNNING("running", Res.string.exercise_running);

    companion object {
        fun fromValue(value: String): ExerciseNameKey {
            return ExerciseNameKey.entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown value: $value")
        }
    }
}

data class Exercise(
    val id: Long,
    val nameKey: ExerciseNameKey,
    val type: ExerciseType,
    val pointsCoefficient: Double // Коэффициент для расчета очков
)