@file:OptIn(ExperimentalTime::class)

package ru.chuvash.reprise.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ru.chuvash.reprise.data.WorkoutRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class StatsPeriodTab { DAY, WEEK, MONTH }
data class ChartDataPoint(val label: String, val value: Float, val fullLabel: String = label)

data class StatItem(val label: String, val value: String)

data class StatisticsState(
    val selectedTab: StatsPeriodTab = StatsPeriodTab.WEEK,
    val currentDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val chartData: List<ChartDataPoint> = emptyList(),
    val summaryStats: List<StatItem> = emptyList(),
    val periodLabel: String = "",
    val isLoading: Boolean = true
)

class StatisticsViewModel(private val repository: WorkoutRepository) : BaseViewModel() {
    private val _state = MutableStateFlow(StatisticsState())
    val state = _state.asStateFlow()
    val todayDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    init {
        loadDataForCurrentState()
    }

    fun onTabSelected(tab: StatsPeriodTab) {
        _state.update {
            it.copy(
                selectedTab = tab,
                currentDate = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            )
        }
        loadDataForCurrentState()
    }

    fun onNextPeriod() {
        _state.update {
            val newDate = when (it.selectedTab) {
                StatsPeriodTab.DAY -> it.currentDate.plus(1, DateTimeUnit.DAY)
                StatsPeriodTab.WEEK -> it.currentDate.plus(1, DateTimeUnit.WEEK)
                StatsPeriodTab.MONTH -> it.currentDate.plus(1, DateTimeUnit.MONTH)
            }
            if (newDate > Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            ) it else it.copy(currentDate = newDate)
        }
        loadDataForCurrentState()
    }

    fun onPreviousPeriod() {
        _state.update {
            val newDate = when (it.selectedTab) {
                StatsPeriodTab.DAY -> it.currentDate.minus(1, DateTimeUnit.DAY)
                StatsPeriodTab.WEEK -> it.currentDate.minus(1, DateTimeUnit.WEEK)
                StatsPeriodTab.MONTH -> it.currentDate.minus(1, DateTimeUnit.MONTH)
            }
            it.copy(currentDate = newDate)
        }
        loadDataForCurrentState()
    }

    private fun loadDataForCurrentState() {
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(isLoading = true) }
            val currentState = _state.value
            val date = currentState.currentDate

            val periodLabel = when (currentState.selectedTab) {
                StatsPeriodTab.DAY -> date.toDayString()
                StatsPeriodTab.WEEK -> date.toWeekRangeString()
                StatsPeriodTab.MONTH -> "${
                    date.month.name.lowercase().replaceFirstChar { it.uppercase() }
                } ${date.year}"
            }

            when (currentState.selectedTab) {
                StatsPeriodTab.DAY -> loadDayData(date)
                StatsPeriodTab.WEEK -> loadWeekData(date)
                StatsPeriodTab.MONTH -> loadMonthData(date)
            }
            _state.update { it.copy(periodLabel = periodLabel, isLoading = false) }
        }
    }

    private fun loadDayData(date: LocalDate) {
        val summary = repository.getExerciseSummaryForDay(date.toString())
        val totalPoints = summary.sumOf { it.totalPoints }
        _state.update {
            it.copy(
                chartData = summary.map { ex ->
                    ChartDataPoint(
                        ex.nameKey.take(3).uppercase(),
                        ex.totalPoints.toFloat(),
                        ex.nameKey
                    )
                },
                summaryStats = listOf(
                    StatItem("Всего очков", totalPoints.toString()),
                    StatItem("Всего упражнений", summary.size.toString())
                )
            )
        }
    }

    private fun loadWeekData(date: LocalDate) {
        val (startDate, endDate) = date.getWeekRange()
        val summary = repository.getDailySummaryForWeek(startDate.toString(), endDate.toString())
        val totalPoints = summary.sumOf { it.totalPoints }

        val daysOfWeek = (0..6).map { startDate.plus(it, DateTimeUnit.DAY) }
        val chartData = daysOfWeek.map { day ->
            val points = summary.find { LocalDate.parse(it.day) == day }?.totalPoints ?: 0
            ChartDataPoint(day.dayOfWeek.name.take(2), points.toFloat())
        }

        _state.update {
            it.copy(
                chartData = chartData,
                summaryStats = listOf(
                    StatItem("Всего очков", totalPoints.toString()),
                    StatItem(
                        "Лучший день",
                        (summary.maxByOrNull { it.totalPoints }?.totalPoints ?: 0).toString()
                    ),
                    StatItem("Активных дней", summary.size.toString())
                )
            )
        }
    }

    private fun loadMonthData(date: LocalDate) {
        val yearMonth = "${date.year}-${date.month.number.toString().padStart(2, '0')}"
        val summary = repository.getWeeklySummaryForMonth(yearMonth)
        val totalPoints = summary.sumOf { it.totalPoints }

        _state.update {
            it.copy(
                chartData = summary.map { w ->
                    ChartDataPoint(
                        "W${w.weekId.takeLast(2)}",
                        w.totalPoints.toFloat()
                    )
                },
                summaryStats = listOf(
                    StatItem("Всего очков", totalPoints.toString()),
                    StatItem(
                        "Лучшая неделя",
                        (summary.maxByOrNull { it.totalPoints }?.totalPoints ?: 0).toString()
                    ),
                    StatItem("Активных недель", summary.size.toString())
                )
            )
        }
    }

    private fun LocalDate.toDayString(): String {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return when (this) {
            today -> "Сегодня"
            today.minus(1, DateTimeUnit.DAY) -> "Вчера"
            else -> "$day ${
                this.month.name.lowercase().replaceFirstChar { it.uppercase() }
            } ${this.year}"
        }
    }

    private fun LocalDate.getWeekRange(): Pair<LocalDate, LocalDate> {
        val dayOfWeek = this.dayOfWeek.isoDayNumber
        val startDate = this.minus(dayOfWeek - 1, DateTimeUnit.DAY)
        val endDate = this.plus(7 - dayOfWeek, DateTimeUnit.DAY)
        return startDate to endDate
    }

    private fun LocalDate.toWeekRangeString(): String {
        val (startDate, endDate) = this.getWeekRange()
        return "${startDate.day} ${startDate.month.name.take(3)} - ${endDate.day} ${
            endDate.month.name.take(
                3
            )
        }"
    }
}
