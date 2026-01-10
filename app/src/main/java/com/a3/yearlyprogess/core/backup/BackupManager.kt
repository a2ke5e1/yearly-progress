package com.a3.yearlyprogess.core.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import com.a3.yearlyprogess.app.MainActivity
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess


class BackupManager(
    private val context: Context,
    private val appSettingsRepository: AppSettingsRepository,
    private val json: Json
) {
    private var roomHelper: RoomBackupHelper? = null

    fun setRoomBackupHelper(helper: RoomBackupHelper) {
        roomHelper = helper
    }

    suspend fun backup(outputUri: Uri) {
        val helper = roomHelper ?: throw IllegalStateException("RoomBackupHelper not initialized")

        val tempDir = File(context.cacheDir, "backup_tmp").apply {
            mkdirs()
        }

        try {
            // Create Room backup using custom location
            val roomFile = helper.backupToCustomLocation(tempDir)

            // Export App Settings from DataStore
            val settings = appSettingsRepository.appSettings.first()
            val prefsFile = File(tempDir, "settings.json")
            prefsFile.writeText(json.encodeToString(AppSettings.serializer(), settings))

            // Zip them
            val zipFile = File(context.cacheDir, "backup.ypp")
            ZipUtils.zip(listOf(roomFile, prefsFile), zipFile)

            // Write zip to SAF Uri
            context.contentResolver.openOutputStream(outputUri)?.use { out ->
                zipFile.inputStream().use { it.copyTo(out) }
            }

            // Cleanup
            tempDir.deleteRecursively()
            zipFile.delete()

        } catch (e: Exception) {
            // Clean up on error
            tempDir.deleteRecursively()
            throw e
        }

        // Restart the app properly - must be last step after all operations complete
        restartApp()
    }

    suspend fun restore(inputUri: Uri) {
        val helper = roomHelper ?: throw IllegalStateException("RoomBackupHelper not initialized")

        val tempDir = File(context.cacheDir, "restore_tmp").apply { mkdirs() }

        try {
            // 1. Copy zip from SAF
            val zipFile = File(tempDir, "backup.ypp")
            context.contentResolver.openInputStream(inputUri)?.use { input ->
                zipFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // 2. Unzip
            val extractedFiles = ZipUtils.unzip(zipFile, tempDir)

            val roomFile = extractedFiles.first { it.name.endsWith(".aes") }
            val prefsFile = extractedFiles.first { it.name == "settings.json" }

            // 3. Restore database using custom file
            helper.restoreFromCustomFile(roomFile)

            // 4. Restore App Settings to DataStore
            val settings = json.decodeFromString(AppSettings.serializer(), prefsFile.readText())
            appSettingsRepository.setAppSettings(settings)

            // Cleanup
            tempDir.deleteRecursively()

        } catch (e: Exception) {
            // Clean up on error
            tempDir.deleteRecursively()
            throw e
        }

        // Restart the app properly - must be last step after all operations complete
        restartApp()
    }

    private fun restartApp() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}