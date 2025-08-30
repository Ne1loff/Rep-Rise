@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.presentation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import ru.chuvash.reprise.data.PreferencesRepository
import ru.chuvash.reprise.data.SwipeAction
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.domain.services.BackupManager
import kotlin.time.ExperimentalTime

data class SettingsState(
    val weeklyPlan: Map<DayOfWeek, Int> = emptyMap(),
    val restoreCompleted: Boolean = false,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val repository: WorkoutRepository,
    private val backupManager: BackupManager,
    private val prefsRepository: PreferencesRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun loadWeeklyPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val plan = repository.getWeeklyGoals()
            _uiState.update { it.copy(weeklyPlan = plan, isLoading = false) }
        }
    }

    fun saveDayGoal(day: DayOfWeek, target: Int) {
        viewModelScope.launch {
            repository.saveWeeklyGoal(day, target)
            // Обновляем состояние, чтобы UI сразу отразил изменения
            val updatedPlan = _uiState.value.weeklyPlan.toMutableMap()
            updatedPlan[day] = target
            _uiState.update { it.copy(weeklyPlan = updatedPlan) }
        }
    }

    fun saveCommonGoal(target: Int) {
        viewModelScope.launch {
            DayOfWeek.entries.forEach { day ->
                repository.saveWeeklyGoal(day, target)
            }
            loadWeeklyPlan() // Перезагружаем, чтобы UI обновился
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            val result = backupManager.createBackup()
            val message = result.fold(
                onSuccess = { it },
                onFailure = { it.message ?: "Ошибка" }
            )
            _events.emit(message)
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            val result = backupManager.restoreBackup()
            val message = result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(restoreCompleted = true) }
                    it
                },
                onFailure = { it.message ?: "Ошибка" }
            )
            _events.emit(message)
        }
    }

    fun getPreferences() = prefsRepository

    fun setSwipeAction(action: SwipeAction) {
        prefsRepository.swipeAction = action.name
    }

    fun setDefaultReps(reps: String) {
        prefsRepository.defaultReps = reps
    }
}