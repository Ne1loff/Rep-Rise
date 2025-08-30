package ru.chuvash.reprise.ui

import Exercise
import ExerciseType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ru.chuvash.reprise.presentation.AddEditSetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSetScreen(
    date: LocalDate,
    setId: String?,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddEditSetViewModel = koinInject { parametersOf(date, setId) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Изменить подход" else "Новый подход") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    // ИСПРАВЛЕНИЕ: Кнопка "Сохранить" теперь здесь
                    IconButton(
                        onClick = { viewModel.saveSet() },
                        enabled = state.isSaveEnabled
                    ) {
                        Icon(Icons.Default.Check, "Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseSelector(
                    exercises = state.allExercises,
                    selected = state.selectedExercise,
                    onSelected = { viewModel.onExerciseSelected(it) }
                )

                AnimatedVisibility(visible = state.selectedExercise?.type in listOf(ExerciseType.REPS_ONLY, ExerciseType.REPS_AND_WEIGHT)) {
                    OutlinedTextField(
                        value = state.reps,
                        onValueChange = { viewModel.onRepsChanged(it) },
                        label = { Text("Повторения") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AnimatedVisibility(visible = state.selectedExercise?.type == ExerciseType.REPS_AND_WEIGHT) {
                    OutlinedTextField(
                        value = state.weight,
                        onValueChange = { viewModel.onWeightChanged(it) },
                        label = { Text("Вес (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AnimatedVisibility(visible = state.selectedExercise?.type in listOf(ExerciseType.TIME, ExerciseType.TIME_AND_DISTANCE)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.durationHours,
                            onValueChange = { viewModel.onDurationHoursChanged(it) },
                            label = { Text("Часы") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.durationMinutes,
                            onValueChange = { viewModel.onDurationMinutesChanged(it) },
                            label = { Text("Мин") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.durationSeconds,
                            onValueChange = { viewModel.onDurationSecondsChanged(it) },
                            label = { Text("Сек") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                AnimatedVisibility(visible = state.selectedExercise?.type == ExerciseType.TIME_AND_DISTANCE) {
                    OutlinedTextField(
                        value = state.distance,
                        onValueChange = { viewModel.onDistanceChanged(it) },
                        label = { Text("Дистанция") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            TextButton(
                                onClick = { viewModel.onDistanceUnitChanged() },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(state.distanceUnit.label)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseSelector(
    exercises: List<Exercise>,
    selected: Exercise?,
    onSelected: (Exercise) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "Выберите упражнение",
            onValueChange = {},
            readOnly = true,
            label = { Text("Упражнение") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = {
                        onSelected(exercise)
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}
