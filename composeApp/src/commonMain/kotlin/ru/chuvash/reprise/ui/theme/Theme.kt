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
    primary = Color(0xFF55D6F5),
    onPrimary = Color(0xFF003641),
    primaryContainer = Color(0xFF004E5D),
    secondary = Color(0xFFFFB77C),
    onSecondary = Color(0xFF4D2700),
    secondaryContainer = Color(0xFF6D3900),
    tertiary = Color(0xFFC4C0E9),
    onTertiary = Color(0xFF2E2D4D),
    tertiaryContainer = Color(0xFF454364),
    background = Color(0xFF191C1D),
    onBackground = Color(0xFFE1E3E4),
    surface = Color(0xFF191C1D),
    onSurface = Color(0xFFE1E3E4),
    surfaceVariant = Color(0xFF3F484A),
    onSurfaceVariant = Color(0xFFBFC8CB),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00687B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFAAF1FF),
    secondary = Color(0xFF8E4F00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFF5D5B7D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3DFFF),
    background = Color(0xFFFBFCFD),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFFBFCFD),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDBE4E7),
    onSurfaceVariant = Color(0xFF3F484A),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White
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
            shapes = AppShapes,
            content = content
        )
    }
}