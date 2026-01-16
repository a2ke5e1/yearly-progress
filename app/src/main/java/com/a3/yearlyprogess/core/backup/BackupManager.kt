package com.a3.yearlyprogess.core.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import com.a3.yearlyprogess.app.MainActivity
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
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

    suspend fun backup(outputUri: Uri) = withContext(Dispatchers.IO) {
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
    }

    suspend fun restore(inputUri: Uri) = withContext(Dispatchers.IO) {
        val helper = roomHelper ?: throw IllegalStateException("RoomBackupHelper not initialized")

        val tempDir = File(context.cacheDir, "restore_tmp").apply { mkdirs() }

        try {
            // 1. Copy file from SAF
            val backupFile = File(tempDir, "temp_backup")
            context.contentResolver.openInputStream(inputUri)?.use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // 2. Check if it's a zip (new format) or sqlite/aes (old format)
            if (ZipUtils.isZipFile(backupFile)) {
                // New format: .ypp (zip)
                val zipFile = File(tempDir, "backup.zip")
                backupFile.renameTo(zipFile)
                
                val extractedFiles = ZipUtils.unzip(zipFile, tempDir)

                val roomFile = extractedFiles.firstOrNull { it.name.endsWith(".aes") }
                val prefsFile = extractedFiles.firstOrNull { it.name == "settings.json" }

                // Restore database
                roomFile?.let { helper.restoreFromCustomFile(it) }

                // Restore App Settings
                prefsFile?.let {
                    val settings = json.decodeFromString(AppSettings.serializer(), it.readText())
                    appSettingsRepository.setAppSettings(settings)
                }
            } else {
                // Old format: direct database file (.sqlite3 or .aes)
                helper.restoreFromCustomFile(backupFile)
            }

            // Cleanup
            tempDir.deleteRecursively()

        } catch (e: Exception) {
            // Clean up on error
            tempDir.deleteRecursively()
            throw e
        }
    }

    fun restartApp() {
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
