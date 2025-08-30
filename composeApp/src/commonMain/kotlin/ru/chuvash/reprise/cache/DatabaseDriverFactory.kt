package ru.chuvash.reprise.cache

import app.cash.sqldelight.db.SqlDriver

// expect - означает, что мы ожидаем реализацию этой функции на каждой платформе
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}