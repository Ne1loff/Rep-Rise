@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import ru.chuvash.reprise.data.PreferencesRepository
import ru.chuvash.reprise.data.SwipeAction
import ru.chuvash.reprise.data.model.WorkoutSet
import ru.chuvash.reprise.presentation.DashboardViewModel
import ru.chuvash.reprise.ui.formatters.toLocaleMonthDay
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinInject(),
    prefs: PreferencesRepository = koinInject(),
    initialDate: LocalDate? = null,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAddSet: (LocalDate, String?) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(initialDate) {
        viewModel.setInitialDate(initialDate)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val newlyUnlocked = state.newlyUnlockedAchievements

    // Этот блок будет запускаться каждый раз, когда меняется список новых достижений
    LaunchedEffect(newlyUnlocked) {
        if (newlyUnlocked.isNotEmpty()) {
            val achievement = newlyUnlocked.first()
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Достижение получено: \"${achievement.title}\"",
                    withDismissAction = true
                )
            }
            // Сообщаем ViewModel, что уведомление было показано
            viewModel.onAchievementNotificationShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Rep:Rise") },
                actions = {
                    IconButton(onClick = onNavigateToStatistics) { // <-- Кнопка
                        Icon(Icons.Default.BarChart, "Статистика")
                    }
                    IconButton(onClick = onNavigateToAchievements) { // <-- Кнопка
                        Icon(Icons.Default.EmojiEvents, "Достижения")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "История"
                        )
                    }
                    // Кнопка для перехода в настройки
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddSet(state.displayedDate, null) }) {
                Icon(Icons.Default.Add, "Добавить подход")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DateSwitcher(
                    date = state.displayedDate,
                    onPreviousDay = { viewModel.goToPreviousDay() },
                    onNextDay = { viewModel.goToNextDay() }
                )
                StreakIndicator(streak = state.currentStreak)
                Spacer(modifier = Modifier.height(16.dp))

                state.currentGoal?.let { goal ->
                    GoalProgress(goal.completedPoints, goal.targetPoints)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                WorkoutHistory(
                    sets = state.setsForDate,
                    swipeAction = prefs.getSwipeActionEnum(),
                    onDeleteClick = { setId -> viewModel.deleteWorkoutSet(setId) },
                    onEditClick = { set -> onNavigateToAddSet(state.displayedDate, set.id) }
                )
            }
        }
    }
}

@Composable
private fun DateSwitcher(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    val today = System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dateText = when (date) {
        today -> "Сегодня"
        today.minus(1, DateTimeUnit.DAY) -> "Вчера"
        else -> {
            val formattedDate = date.toLocaleMonthDay()
            if (date.year != today.year) {
                "$formattedDate ${date.year}"
            } else {
                formattedDate
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(Icons.Default.ChevronLeft, "Предыдущий день")
        }
        Text(dateText, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onNextDay, enabled = date < today) {
            Icon(Icons.Default.ChevronRight, "Следующий день")
        }
    }
}

@Composable
fun GoalProgress(completed: Int, target: Int) {
    val progress = if (target > 0) completed.toFloat() / target.toFloat() else 0f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Цель на сегодня", style = MaterialTheme.typography.titleMedium)
        Text(
            "$completed / $target",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun WorkoutHistory(
    sets: List<WorkoutSet>,
    swipeAction: SwipeAction,
    onDeleteClick: (String) -> Unit,
    onEditClick: (WorkoutSet) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Сегодняшние подходы", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sets, key = { it.id }) { set ->
                var showMenu by remember { mutableStateOf(false) }
                var isDismissed by remember { mutableStateOf(false) }

                // Состояние для SwipeToDismiss
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            when (swipeAction) {
                                SwipeAction.DELETE -> isDismissed = true
                                SwipeAction.EDIT -> onEditClick(set)
                                SwipeAction.NONE -> {}
                            }
                        }
                        false
                    }
                )

                // Запускаем удаление после того, как анимация завершится
                LaunchedEffect(isDismissed) {
                    if (isDismissed) {
                        onDeleteClick(set.id)
                    }
                }

                AnimatedVisibility(
                    visible = !isDismissed,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = swipeAction != SwipeAction.NONE,
                        enableDismissFromStartToEnd = false,
                        modifier = Modifier.clip(CardDefaults.shape),
                        backgroundContent = {
                            val (color, icon) = when (swipeAction) {
                                SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer to Icons.Default.Delete
                                SwipeAction.EDIT -> MaterialTheme.colorScheme.primaryContainer to Icons.Default.Edit
                                SwipeAction.NONE -> MaterialTheme.colorScheme.background to null // Не будет видно
                            }

                            if (icon != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(icon, contentDescription = null)
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { showMenu = true }
                                )
                            }
                        ) {
                            WorkoutSetCard(set)
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Изменить") },
                                    onClick = {
                                        onEditClick(set)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Удалить") },
                                    onClick = {
                                        isDismissed = true // Запускаем анимацию и удаление
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Карточку вынесли в отдельный Composable для чистоты
@Composable
private fun WorkoutSetCard(set: WorkoutSet) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${set.reps} ${set.exercise.name}", fontWeight = FontWeight.Bold)
                val timeString = Instant.fromEpochSeconds(set.timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .time.toString().substringBefore('.')
                Text(timeString, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Новый Composable для отображения стрика
@Composable
private fun StreakIndicator(streak: Int) {
    if (streak > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "$streak ${getStreakDayString(streak)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Вспомогательная функция для правильного склонения слова "день"
private fun getStreakDayString(days: Int): String {
    return when {
        days % 10 == 1 && days % 100 != 11 -> "день"
        days % 10 in 2..4 && days % 100 !in 12..14 -> "дня"
        else -> "дней"
    }
}
