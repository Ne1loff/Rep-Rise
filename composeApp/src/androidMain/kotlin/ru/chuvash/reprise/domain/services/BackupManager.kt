package ru.chuvash.reprise.domain.services

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.chuvash.reprise.utils.FilePickerManager

actual class BackupManager(
    private val context: Context,
    private val driver: SqlDriver,
    private val filePickerManager: FilePickerManager
) {
    private val dbName = "reprise.db"

    actual suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) return@withContext Result.failure(Exception("Файл базы данных не найден."))

            val uri = filePickerManager.createFile("reprise_backup.db")
                ?: return@withContext Result.failure(Exception("Операция отменена пользователем."))

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                dbFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Result.success("Резервная копия успешно создана.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun restoreBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uri = filePickerManager.openFile()
                ?: return@withContext Result.failure(Exception("Операция отменена пользователем."))

            val dbFile = context.getDatabasePath(dbName)

            // Важно! Закрываем соединение с БД перед заменой файла
            driver.close()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                dbFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Result.success("Данные успешно восстановлены. Пожалуйста, перезапустите приложение.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}