package ru.chuvash.reprise.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import org.koin.compose.koinInject
import ru.chuvash.reprise.data.SwipeAction
import ru.chuvash.reprise.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val prefs = remember { viewModel.getPreferences() }
    val snackbarHostState = remember { SnackbarHostState() }

    var swipeAction by remember { mutableStateOf(prefs.getSwipeActionEnum()) }
    var defaultReps by remember { mutableStateOf(prefs.defaultReps) }

    val state by viewModel.uiState.collectAsState()
    val weeklyGoals = state.weeklyPlan.values

    // План считается "общим", если все значения одинаковые (или если он пустой)
    val isCommonPlan = weeklyGoals.isEmpty() || weeklyGoals.toSet().size <= 1
    val commonGoalValue = if (isCommonPlan) weeklyGoals.firstOrNull() ?: 0 else null

    // По умолчанию детальный план свернут, если он "общий"
    var isWeeklyPlanExpanded by remember(isCommonPlan) { mutableStateOf(!isCommonPlan) }

    // Блокируем системную кнопку "назад", если восстановление завершено
    BackHandler(enabled = state.restoreCompleted) {
        // Ничего не делаем, чтобы заблокировать действие
    }

    LaunchedEffect(Unit) {
        if (!state.restoreCompleted) {
            viewModel.loadWeeklyPlan()
        }
        viewModel.events.collectLatest { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Настроки") },
                navigationIcon = {
                    // Скрываем кнопку "назад" после восстановления
                    if (!state.restoreCompleted) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "Назад")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.restoreCompleted) {
            RestoreCompletedOverlay()
        } else if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("План на неделю", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    // Показываем либо общую настройку, либо заголовок детальной
                    if (isWeeklyPlanExpanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Детальная настройка", style = MaterialTheme.typography.bodyLarge)
                            ExpandButton(
                                isExpanded = true,
                                onClick = { isWeeklyPlanExpanded = false }
                            )
                        }
                    } else {
                        DayGoalEditor(
                            label = "Общая цель на день",
                            target = commonGoalValue ?: 0,
                            onTargetChange = { newTarget ->
                                viewModel.saveCommonGoal(newTarget)
                            },
                            trailingIcon = {
                                ExpandButton(
                                    isExpanded = false,
                                    onClick = { isWeeklyPlanExpanded = true }
                                )
                            }
                        )
                    }
                }

                // Показываем детальный список, только если он развернут
                if (isWeeklyPlanExpanded) {
                    items(DayOfWeek.entries.toTypedArray()) { day ->
                        DayGoalEditor(
                            label = day.localizedName(),
                            target = state.weeklyPlan[day] ?: 0,
                            onTargetChange = { newTarget ->
                                viewModel.saveDayGoal(day, newTarget)
                            }
                        )
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Настройки приложения", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))

                    // Настройка свайпа
                    SwipeActionSelector(
                        selectedAction = swipeAction,
                        onActionSelected = {
                            swipeAction = it
                            viewModel.setSwipeAction(it)
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Настройка повторений по умолчанию
                    OutlinedTextField(
                        value = defaultReps,
                        onValueChange = {
                            val filtered = it.filter { char -> char.isDigit() }
                            defaultReps = filtered
                            viewModel.setDefaultReps(filtered)
                        },
                        label = { Text("Количество по умолчанию") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Резервное копирование", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.createBackup() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Создать резервную копию")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.restoreBackup() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Восстановить из копии")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandButton(isExpanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)
    IconButton(onClick = onClick) {
        Icon(
            Icons.Default.ExpandMore,
            contentDescription = "Развернуть/Свернуть",
            modifier = Modifier.rotate(rotation)
        )
    }
}

// Новый Composable для экрана-заглушки после восстановления
@Composable
private fun RestoreCompletedOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Восстановление завершено",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Пожалуйста, закройте и снова откройте приложение, чтобы применить изменения.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayGoalEditor(
    label: String, // Теперь мы передаем название ("Общая цель" или "Понедельник")
    target: Int,
    onTargetChange: (Int) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null // Необязательная иконка (для стрелочки)
) {
    var text by remember(target) { mutableStateOf(if (target > 0) target.toString() else "") }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text,
        onValueChange = { text = it.filter { char -> char.isDigit() } },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, // Используем переданное название
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onTargetChange(text.toIntOrNull() ?: 0)
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        trailingIcon = trailingIcon // Отображаем иконку, если она есть
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeActionSelector(
    selectedAction: SwipeAction,
    onActionSelected: (SwipeAction) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Text("Действие по свайпу", style = MaterialTheme.typography.bodyLarge)
    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
    ) {
        OutlinedTextField(
            value = selectedAction.toRussian(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            SwipeAction.entries.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.toRussian()) },
                    onClick = {
                        onActionSelected(action)
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}

private fun SwipeAction.toRussian(): String = when (this) {
    SwipeAction.DELETE -> "Удалить"
    SwipeAction.EDIT -> "Изменить"
    SwipeAction.NONE -> "Ничего"
}

private fun DayOfWeek.localizedName(): String = when (this) {
    DayOfWeek.MONDAY -> "Понедельник"
    DayOfWeek.TUESDAY -> "Вторник"
    DayOfWeek.WEDNESDAY -> "Среда"
    DayOfWeek.THURSDAY -> "Четверг"
    DayOfWeek.FRIDAY -> "Пятница"
    DayOfWeek.SATURDAY -> "Суббота"
    DayOfWeek.SUNDAY -> "Воскресенье"
}