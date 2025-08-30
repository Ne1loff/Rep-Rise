package ru.chuvash.reprise.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Кастомные цвета для календаря
val LightGreen = Color(0xFFC8E6C9)
val DarkGreen = Color(0xFF388E3C)
val LightYellow = Color(0xFFFFFDE7)
val DarkYellow = Color(0xFFFBC02D)

// Стандартные палитры
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFFB00020)
)

// Data class для хранения кастомных цветов
data class CustomColors(
    val completed: Color,
    val inProgress: Color
)

// CompositionLocal для передачи кастомных цветов по дереву компонентов
val HistorySetsColors = staticCompositionLocalOf {
    CustomColors(
        completed = Color.Unspecified,
        inProgress = Color.Unspecified
    )
}