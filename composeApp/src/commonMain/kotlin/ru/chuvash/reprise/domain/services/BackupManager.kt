package ru.chuvash.reprise.domain.services

expect class BackupManager {
    suspend fun createBackup(): Result<String>
    suspend fun restoreBackup(): Result<String>
}