@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plusMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.data.model.DailyGoal
import ru.chuvash.reprise.data.model.WorkoutSet
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class HistoryState(
    val yearMonth: YearMonth = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.yearMonth,
    val days: Map<LocalDate, DailyGoal> = emptyMap(),
    val selectedDate: LocalDate? = null, // <-- Дата, выбранная пользователем
    val selectedDateSets: List<WorkoutSet> = emptyList(), // <-- Подходы для выбранной даты
    val isLoading: Boolean = true
)

class HistoryViewModel(private val repository: WorkoutRepository) : BaseViewModel() {
    private val _uiState = MutableStateFlow(HistoryState())
    val uiState = _uiState.asStateFlow()
    val todayYearMonth = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.yearMonth

    init {
        loadHistoryForCurrentMonth()
    }

    private fun loadHistoryForCurrentMonth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val yearMonthStr = "${_uiState.value.yearMonth.year}-${
                _uiState.value.yearMonth.month.number.toString().padStart(2, '0')
            }"
            val goals = repository.getGoalsForMonth(yearMonthStr)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    days = goals.associateBy { goal -> LocalDate.parse(goal.date) }
                )
            }
        }
    }

    // Новая функция для обработки нажатия на день
    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            // Если нажать на уже выбранную дату, выбор снимается
            if (_uiState.value.selectedDate == date) {
                _uiState.update { it.copy(selectedDate = null, selectedDateSets = emptyList()) }
                return@launch
            }

            val sets = repository.getSetsForDate(date)
            _uiState.update {
                it.copy(
                    selectedDate = date,
                    selectedDateSets = sets
                )
            }
        }
    }

    fun previousMonth() {
        viewModelScope.launch {
            _uiState.update { it.copy(yearMonth = it.yearMonth.minusMonth()) }
        }
    }

    fun nextMonth() {
        viewModelScope.launch {
            if (_uiState.value.yearMonth < todayYearMonth) {
                _uiState.update { it.copy(yearMonth = it.yearMonth.plusMonth()) }
            }
        }
    }
}