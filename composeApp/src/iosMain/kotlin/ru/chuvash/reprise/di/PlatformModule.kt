package ru.chuvash.reprise.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.chuvash.reprise.cache.DatabaseDriverFactory

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory().createDriver() }
}