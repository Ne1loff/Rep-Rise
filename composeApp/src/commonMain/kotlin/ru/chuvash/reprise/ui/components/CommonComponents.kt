package ru.chuvash.reprise.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import reprise.composeapp.generated.resources.Res
import reprise.composeapp.generated.resources.navigation_previous
import org.jetbrains.compose.resources.stringResource as res

@Composable
fun DateRangeSwitcher(
    label: String,
    labelStyle: TextStyle = MaterialTheme.typography.titleMedium,
    previousDisabled: Boolean = false,
    nextDisabled: Boolean = false,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious, enabled = !previousDisabled) {
            Icon(
                Icons.Default.ChevronLeft,
                res(Res.string.navigation_previous)
            )
        }
        Text(label, style = labelStyle)
        IconButton(onClick = onNext, enabled = !nextDisabled) {
            Icon(
                Icons.Default.ChevronRight,
                res(Res.string.navigation_previous)
            )
        }
    }
}