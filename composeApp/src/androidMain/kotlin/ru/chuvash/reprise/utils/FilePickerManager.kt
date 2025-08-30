package ru.chuvash.reprise.utils

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FilePickerManager(private val activity: ComponentActivity) : DefaultLifecycleObserver {

    private lateinit var createFileLauncher: ActivityResultLauncher<String>
    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>

    private var onFileCreatedContinuation: ((Uri?) -> Unit)? = null
    private var onFileOpenedContinuation: ((Uri?) -> Unit)? = null

    init {
        // Добавляем этот класс в наблюдатели жизненного цикла Activity
        activity.lifecycle.addObserver(this)
    }

    // Этот метод будет вызван системой в правильный момент (до STARTED)
    override fun onCreate(owner: LifecycleOwner) {
        createFileLauncher =
            activity.registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
                onFileCreatedContinuation?.invoke(uri)
                onFileCreatedContinuation = null
            }
        openFileLauncher =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                onFileOpenedContinuation?.invoke(uri)
                onFileOpenedContinuation = null
            }
    }

    suspend fun createFile(fileName: String): Uri? = suspendCancellableCoroutine { continuation ->
        onFileCreatedContinuation = { uri ->
            if (continuation.isActive) continuation.resume(uri)
        }
        createFileLauncher.launch(fileName)
        continuation.invokeOnCancellation { onFileCreatedContinuation = null }
    }

    suspend fun openFile(): Uri? = suspendCancellableCoroutine { continuation ->
        onFileOpenedContinuation = { uri ->
            if (continuation.isActive) continuation.resume(uri)
        }
        openFileLauncher.launch(arrayOf("application/octet-stream"))
        continuation.invokeOnCancellation { onFileOpenedContinuation = null }
    }
}