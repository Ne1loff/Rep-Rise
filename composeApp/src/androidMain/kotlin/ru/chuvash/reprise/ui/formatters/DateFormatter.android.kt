package ru.chuvash.reprise.ui.formatters

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
actual fun LocalDate.toLocaleByPattern(pattern: String): String {
    val javaDate = this.toJavaLocalDate()
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return javaDate.format(formatter)
}
