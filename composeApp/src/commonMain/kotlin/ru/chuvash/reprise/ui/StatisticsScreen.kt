@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.chuvash.reprise.presentation.ChartDataPoint
import ru.chuvash.reprise.presentation.StatisticsViewModel
import ru.chuvash.reprise.presentation.StatsPeriodTab
import ru.chuvash.reprise.ui.components.AnimatedCounter
import ru.chuvash.reprise.ui.components.DateRangeSwitcher
import ru.chuvash.reprise.ui.components.StatCard
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Статистика") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Назад"
                    )
                }
            }
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                StatsPeriodTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(tab.name) } // TODO: Localize
                    )
                }
            }

            DateRangeSwitcher(
                label = state.periodLabel,
                onPrevious = { viewModel.onPreviousPeriod() },
                onNext = { viewModel.onNextPeriod() },
                nextDisabled = state.currentDate == viewModel.todayDate
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.chartData.isNotEmpty()) {

                when (state.selectedTab) {
                    StatsPeriodTab.DAY -> DonutChart(data = state.chartData)
                    else -> BarChart(data = state.chartData)
                }

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.summaryStats.forEach { stat ->
                        StatCard(label = stat.label, value = stat.value)
                    }
                }

            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет данных за этот период.")
                }
            }
        }
    }
}

@Composable
private fun DonutChart(data: List<ChartDataPoint>) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                data.forEachIndexed { index, point ->
                    val sweepAngle = ((point.value / total) * 360f) * animatedProgress.value
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 40f)
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedCounter(
                    count = total.roundToInt(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text("очков", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.width(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            items(data.size) { index ->
                val point = data[index]
                val percentage = (point.value / total * 100).roundToInt()
                LegendItem(
                    color = colors[index % colors.size],
                    label = point.fullLabel, // TODO: Localize
                    percentage = percentage
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, percentage: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(12.dp).background(color, shape = MaterialTheme.shapes.small))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(
                "$percentage%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun BarChart(data: List<ChartDataPoint>) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    // ИСПРАВЛЕНИЕ: Создаем список Animatable для каждого столбца.
    val animatedProgresses = remember(data) {
        data.map { Animatable(0f) }
    }

    // Этот эффект запускается один раз при появлении графика (или смене данных)
    // и анимирует каждый столбец к его целевому значению.
    LaunchedEffect(data) {
        animatedProgresses.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = data[index].value,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
        }
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 0f

    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bottomPadding = 30.dp.toPx()
            val chartHeight = size.height - bottomPadding
            val barSpacing = 8.dp.toPx()
            val barWidth =
                if (data.isNotEmpty()) (size.width - (data.size - 1) * barSpacing) / data.size else 0f

            // Draw grid lines
            (0..4).forEach { i ->
                val y = chartHeight * (i / 4f)
                drawLine(
                    gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            animatedProgresses.forEachIndexed { index, animatable ->
                val pointValue = animatable.value // Используем текущее анимированное значение
                val barHeight = if (maxValue > 0) (pointValue / maxValue) * chartHeight else 0f
                val left = index * (barWidth + barSpacing)
                val top = chartHeight - barHeight

                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.8f),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                val valueTextLayout = textMeasurer.measure(
                    text = AnnotatedString(pointValue.roundToInt().toString()),
                    style = TextStyle(fontSize = 12.sp, color = onBackgroundColor)
                )
                if (top > valueTextLayout.size.height + 4.dp.toPx()) {
                    drawText(
                        textLayoutResult = valueTextLayout,
                        topLeft = Offset(
                            x = left + barWidth / 2 - valueTextLayout.size.width / 2,
                            y = top - valueTextLayout.size.height - 4.dp.toPx()
                        )
                    )
                }

                val labelTextLayout = textMeasurer.measure(
                    text = AnnotatedString(data[index].label),
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