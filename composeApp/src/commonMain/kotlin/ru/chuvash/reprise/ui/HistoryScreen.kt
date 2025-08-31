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
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.koin.compose.koinInject
import reprise.composeapp.generated.resources.Res
import reprise.composeapp.generated.resources.common_today
import reprise.composeapp.generated.resources.day_friday_short
import reprise.composeapp.generated.resources.day_monday_short
import reprise.composeapp.generated.resources.day_saturday_short
import reprise.composeapp.generated.resources.day_sunday_short
import reprise.composeapp.generated.resources.day_thursday_short
import reprise.composeapp.generated.resources.day_tuesday_short
import reprise.composeapp.generated.resources.day_wednesday_short
import reprise.composeapp.generated.resources.history_non_sets_for_day
import reprise.composeapp.generated.resources.history_sets_for_day
import reprise.composeapp.generated.resources.navigation_back
import reprise.composeapp.generated.resources.screen_history
import ru.chuvash.reprise.data.model.DailyGoal
import ru.chuvash.reprise.data.model.WorkoutSet
import ru.chuvash.reprise.presentation.HistoryViewModel
import ru.chuvash.reprise.ui.components.WorkoutSetCard
import ru.chuvash.reprise.ui.formatters.toLocaleMonth
import ru.chuvash.reprise.ui.formatters.toLocaleMonthDay
import ru.chuvash.reprise.ui.theme.HistorySetsColors
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.resources.stringResource as res

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
                title = { Text(res(Res.string.screen_history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, res(Res.string.navigation_back))
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
                        res(
                            Res.string.history_sets_for_day,
                            displayedDate?.toLocaleMonthDay() ?: res(Res.string.common_today)
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    if (displayedSets.isNotEmpty()) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(displayedSets) { set ->
                                WorkoutSetCard(set)
                            }
                        }
                    } else {
                        Text(res(Res.string.history_non_sets_for_day))
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

    val weekDays = dayOfWeekResources()

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            weekDays.forEach { dayResource ->
                Text(res(dayResource), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(firstDayOfWeek - 1) { Box {} }

            items(daysInMonth) { dayOfMonth ->
                val date = LocalDate(yearMonth.year, yearMonth.month, dayOfMonth + 1)
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val goal = data[date]
                val isCompleted = goal != null && goal.completedPoints >= goal.targetPoints
                val hasProgress = goal != null && goal.completedPoints > 0
                val isToday = date == today

                val color = when {
                    isCompleted -> HistorySetsColors.current.completed
                    hasProgress -> HistorySetsColors.current.inProgress
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val borderColor = when {
                    date == selectedDate -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(color)
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = MaterialTheme.shapes.small
                        )
                        .pointerInput(date) {
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

private fun dayOfWeekResources(): List<StringResource> {
    return listOf(
        Res.string.day_monday_short,
        Res.string.day_tuesday_short,
        Res.string.day_wednesday_short,
        Res.string.day_thursday_short,
        Res.string.day_friday_short,
        Res.string.day_saturday_short,
        Res.string.day_sunday_short
    )
}
