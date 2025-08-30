@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import org.koin.compose.koinInject
import ru.chuvash.reprise.data.model.DailyGoal
import ru.chuvash.reprise.data.model.WorkoutSet
import ru.chuvash.reprise.presentation.HistoryViewModel
import ru.chuvash.reprise.ui.formatters.toLocaleMonth
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinInject(),
    onNavigateBack: () -> Unit,
    onDateLongPress: (LocalDate) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var displayedDate by remember { mutableStateOf<LocalDate?>(null) }
    var displayedSets by remember { mutableStateOf<List<WorkoutSet>>(emptyList()) }

    LaunchedEffect(state.selectedDate, state.selectedDateSets) {
        if (state.selectedDate != null) {
            displayedDate = state.selectedDate
            displayedSets = state.selectedDateSets
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text(
                "${state.yearMonth.firstDay.toLocaleMonth().uppercase()} ${state.yearMonth.year}",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            CalendarView(
                yearMonth = state.yearMonth,
                data = state.days,
                selectedDate = state.selectedDate,
                onDateSelected = { date -> viewModel.selectDate(date) },
                onDateLongPress = onDateLongPress
            )

            AnimatedVisibility(visible = state.selectedDate != null) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        "Подходы за ${displayedDate?.day} ${displayedDate?.month?.name}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    if (displayedSets.isNotEmpty()) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(displayedSets) { set ->
                                ReadOnlyWorkoutSetCard(set)
                            }
                        }
                    } else {
                        Text("В этот день подходов не было.")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarView(
    yearMonth: YearMonth,
    data: Map<LocalDate, DailyGoal>,
    selectedDate: LocalDate?, // <-- Текущая выбранная дата
    onDateSelected: (LocalDate) -> Unit, // <-- Функция-обработчик нажатия
    onDateLongPress: (LocalDate) -> Unit,
) {
    val firstDayOfMonth = LocalDate(yearMonth.year, yearMonth.month, 1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber // 1 for Monday, 7 for Sunday
    val daysInMonth = yearMonth.numberOfDays

    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            weekDays.forEach { day ->
                Text(day, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Пустые ячейки до начала месяца
            items(firstDayOfWeek - 1) { Box {} }

            items(daysInMonth) { dayOfMonth ->
                val date = LocalDate(yearMonth.year, yearMonth.month, dayOfMonth + 1)
                val goal = data[date]
                val isCompleted = goal != null && goal.completedReps >= goal.targetReps
                val hasData = goal != null && goal.completedReps > 0

                val color = when {
                    isCompleted -> MaterialTheme.colorScheme.primaryContainer
                    hasData -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(color)
                        .border( // <-- Обводка для выделения
                            width = 2.dp,
                            color = if (date == selectedDate) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onDateSelected(date) },
                                onLongPress = { onDateLongPress(date) }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text((dayOfMonth + 1).toString())
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyWorkoutSetCard(set: WorkoutSet) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${set.reps} ${set.exerciseType}", fontWeight = FontWeight.Bold)
                val timeString = set.dateTime.time.toString().substringBefore('.')
                Text(timeString, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
