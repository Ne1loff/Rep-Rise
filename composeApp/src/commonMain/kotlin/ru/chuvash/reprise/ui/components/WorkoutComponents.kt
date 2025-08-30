package ru.chuvash.reprise.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.chuvash.reprise.data.model.WorkoutSet
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutSetCard(set: WorkoutSet) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(set.exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = set.dateTime.time.toString().substringBefore('.'),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${set.effortPoints}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " очков",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        }

        val chipsData = remember(set) {
            buildList {
                set.reps?.let { if (it > 0) add("Повторения: $it") }
                set.weight?.let { if (it > 0) add("Вес: $it кг") }
                set.durationSeconds?.let { if (it > 0) add("Время: ${formatDurationToHms(it)}") }
                set.distanceMeters?.let {
                    if (it > 0) {
                        val (value, unit) = if (it >= 1000) {
                            val km = it / 1000.0
                            val roundedKm = (km * 10).roundToInt() / 10.0
                            roundedKm.toString() to "км"
                        } else {
                            "$it" to "м"
                        }
                        add("Дистанция: $value $unit")
                    }
                }
            }
        }

        if (chipsData.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                chipsData.forEach { label ->
                    AssistChip(
                        onClick = { /* No action */ },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

fun formatDurationToHms(totalSeconds: Int): String {
    if (totalSeconds <= 0) return ""
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val m = minutes.toString().padStart(2, '0')
    val s = seconds.toString().padStart(2, '0')

    return if (hours > 0) {
        "$hours:$m:$s"
    } else {
        "$m:$s"
    }
}