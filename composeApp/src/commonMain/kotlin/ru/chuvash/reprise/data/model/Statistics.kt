package ru.chuvash.reprise.data.model

data class DailySummary(val day: String, val totalPoints: Int)
data class WeeklySummary(val weekId: String, val totalPoints: Int)
data class ExerciseSummary(val nameKey: String, val totalPoints: Int)