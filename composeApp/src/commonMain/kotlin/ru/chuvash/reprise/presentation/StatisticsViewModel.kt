package ru.chuvash.reprise.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.data.model.DailySummary
import ru.chuvash.reprise.data.model.WeeklySummary

enum class StatsPeriod { WEEKS, DAYS }

data class StatisticsState(
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEKS,
    val weeklySummary: List<WeeklySummary> = emptyList(),
    val dailySummary: List<DailySummary> = emptyList(),
    val totalReps: Int = 0,
    val bestWeekReps: Int = 0,
    val averageRepsPerDay: Int = 0,
    val isLoading: Boolean = true
)

class StatisticsViewModel(private val repository: WorkoutRepository) : BaseViewModel() {
    private val _uiState = MutableStateFlow(StatisticsState())
    val uiState = _uiState.asStateFlow()

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val weeklySummary = repository.getWeeklySummary()
            val dailySummary = repository.getDailySummaryForLast30Days()
            val totalReps = repository.getTotalReps()
            val activeDays = repository.getActiveDaysCount()
            val bestWeek = weeklySummary.maxOfOrNull { it.totalReps } ?: 0
            val average = if (activeDays > 0) totalReps / activeDays else 0

            _uiState.update {
                it.copy(
                    weeklySummary = weeklySummary.reversed(),
                    dailySummary = dailySummary.reversed(),
                    totalReps = totalReps,
                    bestWeekReps = bestWeek,
                    averageRepsPerDay = average,
                    isLoading = false
                )
            }
        }
    }

    fun setPeriod(period: StatsPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }
}
