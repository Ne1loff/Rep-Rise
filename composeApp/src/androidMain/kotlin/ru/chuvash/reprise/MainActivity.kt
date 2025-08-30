package ru.chuvash.reprise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import ru.chuvash.reprise.di.activityModule
import ru.chuvash.reprise.utils.FilePickerManager


class MainActivity : ComponentActivity() {

    private val activityModule = activityModule(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        loadKoinModules(activityModule)
        get<FilePickerManager>()

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        unloadKoinModules(activityModule)
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}