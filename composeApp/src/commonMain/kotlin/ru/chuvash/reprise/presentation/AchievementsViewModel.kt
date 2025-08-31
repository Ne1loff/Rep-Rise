package ru.chuvash.reprise.presentation


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.domain.model.Achievement
import ru.chuvash.reprise.domain.model.AchievementsList

data class UiAchievement(
    val achievement: Achievement,
    val isUnlocked: Boolean
)

data class AchievementsState(
    val achievements: List<UiAchievement> = emptyList()
)

class AchievementsViewModel(private val repository: WorkoutRepository) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AchievementsState())
    val uiState = _uiState.asStateFlow()

    fun loadAchievements() {
        viewModelScope.launch {
            val unlockedIds = repository.getUnlockedAchievementIds()
            val uiAchievements = AchievementsList.all
                .filter { !it.isSecret || it.id in unlockedIds }
                .map { UiAchievement(achievement = it, isUnlocked = it.id in unlockedIds) }
                .sortedByDescending { it.isUnlocked }
            _uiState.update { it.copy(achievements = uiAchievements) }
        }
    }
}