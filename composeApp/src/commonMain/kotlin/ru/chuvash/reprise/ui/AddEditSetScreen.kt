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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import reprise.composeapp.generated.resources.Res
import reprise.composeapp.generated.resources.addedit_add_set
import reprise.composeapp.generated.resources.addedit_distance
import reprise.composeapp.generated.resources.addedit_edit_set
import reprise.composeapp.generated.resources.addedit_hours
import reprise.composeapp.generated.resources.addedit_minutes
import reprise.composeapp.generated.resources.addedit_reps
import reprise.composeapp.generated.resources.addedit_seconds
import reprise.composeapp.generated.resources.addedit_select_exercise
import reprise.composeapp.generated.resources.addedit_weight_kg
import reprise.composeapp.generated.resources.common_exercise
import reprise.composeapp.generated.resources.common_save
import reprise.composeapp.generated.resources.navigation_back
import ru.chuvash.reprise.presentation.AddEditSetViewModel
import org.jetbrains.compose.resources.stringResource as res

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
                title = { Text(res(if (state.isEditMode) Res.string.addedit_edit_set else Res.string.addedit_add_set)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, res(Res.string.navigation_back))
                    }
                },
                actions = {
                    // ИСПРАВЛЕНИЕ: Кнопка "Сохранить" теперь здесь
                    IconButton(
                        onClick = { viewModel.saveSet() },
                        enabled = state.isSaveEnabled
                    ) {
                        Icon(Icons.Default.Check, res(Res.string.common_save))
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

                AnimatedVisibility(
                    visible = state.selectedExercise?.type in listOf(
                        ExerciseType.REPS_ONLY,
                        ExerciseType.REPS_AND_WEIGHT
                    )
                ) {
                    OutlinedTextField(
                        value = state.reps,
                        onValueChange = { viewModel.onRepsChanged(it) },
                        label = { Text(res(Res.string.addedit_reps)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AnimatedVisibility(visible = state.selectedExercise?.type == ExerciseType.REPS_AND_WEIGHT) {
                    OutlinedTextField(
                        value = state.weight,
                        onValueChange = { viewModel.onWeightChanged(it) },
                        label = { Text(res(Res.string.addedit_weight_kg)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AnimatedVisibility(
                    visible = state.selectedExercise?.type in listOf(
                        ExerciseType.TIME,
                        ExerciseType.TIME_AND_DISTANCE
                    )
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.durationHours,
                            onValueChange = { viewModel.onDurationHoursChanged(it) },
                            label = { Text(res(Res.string.addedit_hours)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.durationMinutes,
                            onValueChange = { viewModel.onDurationMinutesChanged(it) },
                            label = { Text(res(Res.string.addedit_minutes)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.durationSeconds,
                            onValueChange = { viewModel.onDurationSecondsChanged(it) },
                            label = { Text(res(Res.string.addedit_seconds)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                AnimatedVisibility(visible = state.selectedExercise?.type == ExerciseType.TIME_AND_DISTANCE) {
                    OutlinedTextField(
                        value = state.distance,
                        onValueChange = { viewModel.onDistanceChanged(it) },
                        label = { Text(res(Res.string.addedit_distance)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            TextButton(
                                onClick = { viewModel.onDistanceUnitChanged() },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(res(state.distanceUnit.label))
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
            value = selected?.let { res(it.nameKey.resource) }
                ?: res(Res.string.addedit_select_exercise),
            onValueChange = {},
            readOnly = true,
            label = { Text(res(Res.string.common_exercise)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(res(exercise.nameKey.resource)) },
                    onClick = {
                        onSelected(exercise)
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}
