package ru.chuvash.reprise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Цветовая палитра для темной темы
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFFCF6679),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

// Цветовая палитра для светлой темы
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFFB00020),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColors = if (darkTheme) {
        CustomColors(completed = DarkGreen, inProgress = DarkYellow)
    } else {
        CustomColors(completed = LightGreen, inProgress = LightYellow)
    }

    CompositionLocalProvider(HistorySetsColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}