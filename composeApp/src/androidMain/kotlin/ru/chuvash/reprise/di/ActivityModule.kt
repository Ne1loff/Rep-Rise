package ru.chuvash.reprise.di

import androidx.activity.ComponentActivity
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.chuvash.reprise.domain.services.BackupManager
import ru.chuvash.reprise.utils.FilePickerManager

fun activityModule(activity: ComponentActivity): Module = module {
    single(createdAtStart = true) { FilePickerManager(activity) }

    factory<BackupManager> {
        BackupManager(
            context = get(), // Koin предоставит ApplicationContext
            driver = get(), // Koin предоставит SqlDriver из platformModule
            filePickerManager = get() // Koin предоставит FilePickerManager из этого же модуля
        )
    }
}