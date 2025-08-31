package ru.chuvash.reprise.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import org.koin.compose.koinInject
import ru.chuvash.reprise.presentation.StatisticsState
import ru.chuvash.reprise.presentation.StatisticsViewModel
import ru.chuvash.reprise.presentation.StatsPeriod

// Универсальная модель для точки на графике
data class ChartDataPoint(val label: String, val value: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.weeklySummary.isNotEmpty() || state.dailySummary.isNotEmpty()) {
                PeriodSelector(
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) }
                )
                Spacer(Modifier.height(24.dp))

                val chartData = remember(state.selectedPeriod, state.weeklySummary, state.dailySummary) {
                    when (state.selectedPeriod) {
                        StatsPeriod.WEEKS -> state.weeklySummary.map {
                            ChartDataPoint(label = weekIdToDateRange(it.weekId), value = it.totalReps)
                        }
                        StatsPeriod.DAYS -> state.dailySummary.map {
                            val parts = it.dayId.split('-')
                            val label = if (parts.size == 3) "${parts[2]}.${parts[1]}" else it.dayId
                            ChartDataPoint(label = label, value = it.totalReps)
                        }
                    }
                }
                BarChart(chartData)
                Spacer(Modifier.height(32.dp))
                Text("Общая статистика (за все время)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                StatisticsSummary(state)
            } else {
                Text("Пока нет данных для отображения.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selectedPeriod == StatsPeriod.WEEKS,
            onClick = { onPeriodSelected(StatsPeriod.WEEKS) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) { Text("Недели") }
        SegmentedButton(
            selected = selectedPeriod == StatsPeriod.DAYS,
            onClick = { onPeriodSelected(StatsPeriod.DAYS) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) { Text("Дни (30)") }
    }
}

@Composable
private fun BarChart(data: List<ChartDataPoint>) {
    val maxValue = data.maxOfOrNull { it.value }?.toFloat() ?: 0f
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bottomPadding = 30.dp.toPx()
            val chartHeight = size.height - bottomPadding
            val barSpacing = 8.dp.toPx()
            val barWidth = (size.width - (data.size - 1) * barSpacing) / data.size

            data.forEachIndexed { index, point ->
                val barHeight = if (maxValue > 0) (point.value / maxValue) * chartHeight else 0f
                val left = index * (barWidth + barSpacing)
                val top = chartHeight - barHeight

                drawRect(
                    color = primaryColor,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )

                val repsTextLayout = textMeasurer.measure(
                    text = AnnotatedString(point.value.toString()),
                    style = TextStyle(fontSize = 12.sp, color = onBackgroundColor)
                )
                drawText(
                    textLayoutResult = repsTextLayout,
                    topLeft = Offset(
                        x = left + barWidth / 2 - repsTextLayout.size.width / 2,
                        y = top - repsTextLayout.size.height - 4.dp.toPx()
                    )
                )

                val labelTextLayout = textMeasurer.measure(
                    text = AnnotatedString(point.label),
                    style = TextStyle(fontSize = 10.sp, color = onBackgroundColor)
                )
                drawText(
                    textLayoutResult = labelTextLayout,
                    topLeft = Offset(
                        x = left + barWidth / 2 - labelTextLayout.size.width / 2,
                        y = chartHeight + 4.dp.toPx()
                    )
                )
            }
        }
    }
}

@Composable
private fun StatisticsSummary(state: StatisticsState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Всего очков", state.totalPoints.toString())
        StatCard("Лучшая неделя", state.bestWeekReps.toString())
        StatCard("Среднее в день", state.averageRepsPerDay.toString())
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

private fun weekIdToDateRange(weekId: String): String {
    try {
        val (year, week) = weekId.split('-').map { it.toInt() }
        val firstDayOfYear = LocalDate(year, 1, 1)
        val firstMonday = firstDayOfYear.plus((8 - firstDayOfYear.dayOfWeek.isoDayNumber) % 7, DateTimeUnit.DAY)
        val mondayOfWeek = firstMonday.plus(week - 1, DateTimeUnit.WEEK)
        val sundayOfWeek = mondayOfWeek.plus(6, DateTimeUnit.DAY)
        val mondayStr = "${mondayOfWeek.dayOfMonth.toString().padStart(2, '0')}.${mondayOfWeek.monthNumber.toString().padStart(2, '0')}"
        val sundayStr = "${sundayOfWeek.dayOfMonth.toString().padStart(2, '0')}.${sundayOfWeek.monthNumber.toString().padStart(2, '0')}"
        return "$mondayStr\n$sundayStr" // Добавляем перенос строки для лучшего отображения
    } catch (e: Exception) {
        return "W${weekId.substringAfter('-')}"
    }
}
