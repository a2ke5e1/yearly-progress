package com.a3.yearlyprogess.core.backup

import android.content.Context
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.io.File

class RoomBackupHelper(
    private val context: Context,
    private val roomBackup: RoomBackup
) {

    /**
     * Backup database to a custom location.
     * RoomBackup will create the backup in its default location, then we copy it to our custom location.
     */
    fun backupToCustomLocation(customDir: File): File {
        // Trigger the backup - RoomBackup will create it in its internal backup directory
        roomBackup.backup()

        // RoomBackup always stores backups in this location
        val defaultBackupDir = File(context.filesDir, "databasebackup")

        if (!defaultBackupDir.exists()) {
            throw IllegalStateException("Room backup directory not found after backup")
        }

        // Get the latest backup file (the one we just created)
        val latestBackup = defaultBackupDir
            .listFiles { file -> file.name.endsWith(".sqlite3.aes") }
            ?.maxByOrNull { it.lastModified() }
            ?: throw IllegalStateException("Room backup file was not created")

        // Copy to our custom location
        val customBackupFile = File(customDir, "database.aes")
        latestBackup.copyTo(customBackupFile, overwrite = true)

        // Clean up RoomBackup's default directory to avoid issues
        defaultBackupDir.deleteRecursively()

        return customBackupFile
    }

    /**
     * Restore database from a custom file.
     * We need to copy the file to a location where RoomBackup can access it,
     * then use RoomBackup's custom file restore mechanism.
     */
    fun restoreFromCustomFile(backupFile: File) {
        // Create a temporary location for the restore file
        val restoreDir = File(context.cacheDir, "restore_temp").apply { mkdirs() }
        val restoreFile = File(restoreDir, "restore.sqlite3.aes")

        // Copy the backup file to the temp location
        backupFile.copyTo(restoreFile, overwrite = true)

        // Configure RoomBackup to use our custom file
        roomBackup.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE)
        roomBackup.backupLocationCustomFile(restoreFile)

        // Trigger the restore - RoomBackup will read from our custom file
        roomBackup.restore()

        // Clean up
        restoreDir.deleteRecursively()
    }
}