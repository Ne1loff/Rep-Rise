package ru.chuvash.reprise.ui

import Exercise
import ExerciseType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSetScreen(
    // viewModel: AddEditSetViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    // Временные данные для демонстрации
    val exercises = listOf(
        Exercise(1, "Отжимания", ExerciseType.REPS_ONLY, 1.0),
        Exercise(2, "Жим лежа", ExerciseType.REPS_AND_WEIGHT, 0.05),
        Exercise(3, "Планка", ExerciseType.TIME, 0.5),
        Exercise(4, "Бег", ExerciseType.TIME_AND_DISTANCE, 0.05) // ИСПРАВЛЕНИЕ: Новое упражнение
    )
    var selectedExercise by remember { mutableStateOf(exercises.first()) }

    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый подход") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: save */ onNavigateBack() }) {
                Text("Сохранить")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Выбор упражнения (здесь будет ExposedDropdownMenuBox)
            OutlinedTextField(
                value = selectedExercise.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Упражнение") },
                modifier = Modifier.fillMaxWidth()
            )

            // Динамические поля
            if (selectedExercise.type == ExerciseType.REPS_ONLY || selectedExercise.type == ExerciseType.REPS_AND_WEIGHT) {
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Повторения") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (selectedExercise.type == ExerciseType.REPS_AND_WEIGHT) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Вес (кг)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (selectedExercise.type == ExerciseType.TIME || selectedExercise.type == ExerciseType.TIME_AND_DISTANCE) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Длительность (сек)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (selectedExercise.type == ExerciseType.TIME_AND_DISTANCE) {
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Дистанция (м)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
