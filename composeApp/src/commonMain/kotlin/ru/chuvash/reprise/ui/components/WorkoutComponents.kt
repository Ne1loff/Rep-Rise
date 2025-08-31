package ru.chuvash.reprise.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import reprise.composeapp.generated.resources.Res
import reprise.composeapp.generated.resources.common_error
import reprise.composeapp.generated.resources.component_workout_distance
import reprise.composeapp.generated.resources.component_workout_duration
import reprise.composeapp.generated.resources.component_workout_points
import reprise.composeapp.generated.resources.component_workout_reps
import reprise.composeapp.generated.resources.component_workout_weight_kg
import ru.chuvash.reprise.data.model.WorkoutSet
import ru.chuvash.reprise.presentation.DistanceUnit
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource as res

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutSetCard(set: WorkoutSet) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(set.exercise.nameKey.resource),
                    style = MaterialTheme.typography.titleMedium
                )
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
                    text = pluralStringResource(
                        Res.plurals.component_workout_points,
                        quantity = set.effortPoints
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        }

        val chipsData: List<Pair<StringResource, Array<Any>>> = remember(set) {
            buildList {
                set.reps?.let { if (it > 0) add(Res.string.component_workout_reps to arrayOf(it)) }
                set.weight?.let {
                    if (it > 0) add(
                        Res.string.component_workout_weight_kg to arrayOf(
                            it
                        )
                    )
                }
                set.durationSeconds?.let {
                    if (it > 0) add(
                        Res.string.component_workout_duration to arrayOf(
                            formatDurationToHms(it)
                        )
                    )
                }
                set.distanceMeters?.let {
                    if (it > 0) {
                        val (value, unit) = if (it >= 1000) {
                            val km = it / 1000.0
                            val roundedKm = (km * 10).roundToInt() / 10.0
                            roundedKm to DistanceUnit.KILOMETERS
                        } else {
                            it to DistanceUnit.METERS
                        }
                        add(Res.string.component_workout_distance to arrayOf(value, unit.label))
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
                chipsData.forEach { (resource, args) ->
                    val formatedArgs =
                        args.map { computeIfResourceOrReturn(it) }.toTypedArray<Any>()
                    AssistChip(
                        onClick = { /* No action */ },
                        label = {
                            Text(
                                res(resource, *formatedArgs),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun computeIfResourceOrReturn(mayBeResource: Any): String {
    if (mayBeResource !is Resource) return mayBeResource.toString()
    return when (mayBeResource) {
        is StringResource -> stringResource(mayBeResource)
        else -> stringResource(Res.string.common_error)
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