package ru.chuvash.reprise.di

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.chuvash.reprise.cache.DatabaseDriverFactory

actual fun platformModule(): Module = module {
    single<SqlDriver> { DatabaseDriverFactory(context = get()).createDriver() }
}