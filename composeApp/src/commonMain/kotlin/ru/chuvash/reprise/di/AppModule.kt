package ru.chuvash.reprise.di

import com.russhwolf.settings.Settings
import kotlinx.datetime.LocalDate
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import ru.chuvash.reprise.cache.AppDatabase
import ru.chuvash.reprise.data.PreferencesRepository
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.domain.services.AchievementService
import ru.chuvash.reprise.domain.services.WorkoutService
import ru.chuvash.reprise.presentation.AchievementsViewModel
import ru.chuvash.reprise.presentation.AddEditSetViewModel
import ru.chuvash.reprise.presentation.DashboardViewModel
import ru.chuvash.reprise.presentation.HistoryViewModel
import ru.chuvash.reprise.presentation.SettingsViewModel
import ru.chuvash.reprise.presentation.StatisticsViewModel


fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(commonModule())
    }
}

// Общий модуль для commonMain
fun commonModule() = module {
    // Data
    single { Settings() } // Предоставляем экземпляр настроек
    single { AppDatabase(driver = get()) }
    single { WorkoutRepository(database = get()) }
    single { PreferencesRepository(settings = get()) }

    // Domain
    single { AchievementService(repository = get()) }
    factory { WorkoutService() }

    // Presentation (ViewModels)
    factory { DashboardViewModel(repository = get()) }
    factory { SettingsViewModel(repository = get(), backupManager = get(), prefsRepository = get()) }
    factory { HistoryViewModel(repository = get()) }
    factory { AchievementsViewModel(repository = get()) }
    factory { StatisticsViewModel(repository = get()) }
    factory { (date: LocalDate, setId: String?) ->
        AddEditSetViewModel(date, setId, repository = get(), workoutService = get())
    }
}