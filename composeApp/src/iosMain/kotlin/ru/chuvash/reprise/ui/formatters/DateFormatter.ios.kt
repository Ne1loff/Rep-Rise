package ru.chuvash.reprise.ui.formatters

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun LocalDate.toLocaleByPattern(pattern: String): String {
    val components = this.toNSDateComponents()
    val calendar = NSCalendar.currentCalendar
    val date = calendar.dateFromComponents(components) ?: return ""

    val dateFormatter = NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        setLocalizedDateFormatFromTemplate(pattern)
    }
    return dateFormatter.stringFromDate(date)
}