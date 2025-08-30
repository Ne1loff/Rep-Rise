package ru.chuvash.reprise.ui.formatters

import androidx.annotation.RequiresApi
import kotlinx.datetime.LocalDate

expect fun LocalDate.toLocaleByPattern(pattern: String): String

fun LocalDate.toLocaleMonthDay(): String {
    return toLocaleByPattern("d MMMM")
}

fun LocalDate.toLocaleMonth(): String {
    return toLocaleByPattern("MMMM")
}