package ru.chuvash.reprise.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import ru.chuvash.reprise.di.commonModule
import ru.chuvash.reprise.di.initKoin
import ru.chuvash.reprise.di.platformModule

class RepriseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@RepriseApp)
            modules(
                commonModule(),
                platformModule()
            )
        }
    }
}