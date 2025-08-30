@file:OptIn(ExperimentalTime::class)
@file:Suppress("UNCHECKED_CAST")

package ru.chuvash.reprise.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.data.model.DailyGoal
import ru.chuvash.reprise.data.model.WorkoutSet
import ru.chuvash.reprise.domain.model.Achievement
import ru.chuvash.reprise.domain.services.AchievementService
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Для KMP ViewModel обычно создают свой класс или используют готовую библиотеку (MVIKotlin, etc.)
// Для простоты создадим базовый класс.
open class BaseViewModel {
    protected val viewModelScope = CoroutineScope(Dispatchers.Main)
}

/**
 * Состояние главного экрана.
 * @param currentGoal Сегодняшняя цель.
 * @param setsToday Список подходов за сегодня.
 * @param isLoading Идет ли загрузка данных.
 */
data class DashboardState(
    val isLoading: Boolean = true,
    val displayedDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val currentGoal: DailyGoal? = null,
    val setsForDate: List<WorkoutSet> = emptyList(),
    val currentStreak: Int = 0,
    val newlyUnlockedAchievements: List<Achievement> = emptyList()
)

class DashboardViewModel(
    private val repository: WorkoutRepository
) : BaseViewModel() {
    private val _displayedDate =
        MutableStateFlow(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    private val _isLoading = MutableStateFlow(true)
    private val _currentGoal = MutableStateFlow<DailyGoal?>(null)
    private val _setsForDate = MutableStateFlow<List<WorkoutSet>>(emptyList())

    private val _currentStreak = MutableStateFlow(0)
    private val _newlyUnlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())

    val uiState: StateFlow<DashboardState> = combine(
        _displayedDate,
        _isLoading,
        _currentGoal,
        _setsForDate,
        _currentStreak,
        _newlyUnlockedAchievements
    ) { flows ->
        DashboardState(
            displayedDate = flows[0] as LocalDate,
            isLoading = flows[1] as Boolean,
            currentGoal = flows[2] as DailyGoal?,
            setsForDate = flows[3] as List<WorkoutSet>,
            currentStreak = flows[4] as Int,
            newlyUnlockedAchievements = flows[5] as List<Achievement>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    fun setInitialDate(date: LocalDate?) {
        if (date != null && date != _displayedDate.value) {
            _displayedDate.value = getAvailableNextDate(date)
        }
        loadDataForCurrentDate()
    }

    fun goToNextDay() {
        _displayedDate.value = getAvailableNextDate(_displayedDate.value.plus(1, DateTimeUnit.DAY))
        loadDataForCurrentDate()
    }

    fun goToPreviousDay() {
        _displayedDate.value = getAvailableNextDate(_displayedDate.value.minus(1, DateTimeUnit.DAY))
        loadDataForCurrentDate()
    }

    private fun loadDataForCurrentDate() {
        viewModelScope.launch(Dispatchers.Default) {
            _isLoading.value = true
            val date = _displayedDate.value

            val weeklyGoals = repository.getWeeklyGoals()
            val targetForDate = weeklyGoals[date.dayOfWeek] ?: 100

            var goal = repository.getGoalForDate(date)
            if (goal == null) {
                goal = DailyGoal(date = date.toString(), targetPoints = targetForDate)
                repository.saveGoal(goal)
            } else if (goal.targetPoints != targetForDate) {
                goal = goal.copy(targetPoints = targetForDate)
                repository.saveGoal(goal)
            }

            val sets = repository.getSetsForDate(date)
            val streak = repository.calculateCurrentStreak()
            val totalPoints = sets.sumOf { it.effortPoints }

            if (goal.completedPoints != totalPoints) {
                repository.updateCompletedPointsForDate(date, totalPoints)
            }
            val updatedGoal = goal.copy(completedPoints = totalPoints)

            _currentGoal.value = updatedGoal
            _setsForDate.value = sets
            _currentStreak.value = streak
            _isLoading.value = false
        }
    }

    fun deleteWorkoutSet(setId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.deleteWorkoutSetById(setId)
            loadDataForCurrentDate()
        }
    }

    fun onAchievementNotificationShown() {
        _newlyUnlockedAchievements.value = emptyList()
    }

    private fun getAvailableNextDate(nextDate: LocalDate): LocalDate {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return if (nextDate > today) today else nextDate
    }
}
