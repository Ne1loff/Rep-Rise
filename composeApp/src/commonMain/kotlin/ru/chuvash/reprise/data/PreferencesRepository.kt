package ru.chuvash.reprise.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.string

enum class SwipeAction { DELETE, EDIT, NONE }

class PreferencesRepository(private val settings: Settings) {
    var swipeAction by settings.string(key = "swipe_action", defaultValue = SwipeAction.DELETE.name)
    var defaultReps by settings.string(key = "default_reps", defaultValue = "20")

    fun getSwipeActionEnum(): SwipeAction = SwipeAction.valueOf(swipeAction)
}