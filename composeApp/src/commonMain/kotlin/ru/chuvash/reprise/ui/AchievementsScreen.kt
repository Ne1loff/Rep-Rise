package ru.chuvash.reprise.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import reprise.composeapp.generated.resources.Res
import reprise.composeapp.generated.resources.navigation_back
import reprise.composeapp.generated.resources.screen_achievements
import ru.chuvash.reprise.presentation.AchievementsViewModel
import ru.chuvash.reprise.presentation.UiAchievement
import org.jetbrains.compose.resources.stringResource as res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAchievements()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(res(Res.string.screen_achievements)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, res(Res.string.navigation_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.achievements) { uiAchievement ->
                AchievementItem(uiAchievement)
            }
        }
    }
}

@Composable
private fun AchievementItem(uiAchievement: UiAchievement) {
    val alpha = if (uiAchievement.isUnlocked) 1f else 0.5f
    Card(modifier = Modifier.fillMaxWidth().alpha(alpha)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = uiAchievement.achievement.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    res(uiAchievement.achievement.title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    res(uiAchievement.achievement.description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}