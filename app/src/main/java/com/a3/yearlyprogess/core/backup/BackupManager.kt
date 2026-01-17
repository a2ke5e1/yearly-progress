package com.a3.yearlyprogess.core.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import com.a3.yearlyprogess.app.MainActivity
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.time.Instant
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
            deleteRecursively()
            mkdirs()
        }

        try {
            val roomFile = helper.backupToCustomLocation(tempDir)

            val settings = appSettingsRepository.appSettings.first()
            val settingsFile = File(tempDir, "settings.json").apply {
                writeText(json.encodeToString(AppSettings.serializer(), settings))
            }

            // Count images
            val imageFiles = context.filesDir.listFiles()
                ?.filter { it.isFile && it.extension.equals("jpg", true) }
                ?: emptyList()
            val imageCount = imageFiles.size

            val zipEntries = mutableMapOf<String, File>()

            // Root files
            zipEntries[roomFile.name] = roomFile
            zipEntries["settings.json"] = settingsFile

            // JPG images from root filesDir → images/*
            imageFiles.forEach { jpg ->
                zipEntries["images/${jpg.name}"] = jpg
            }

            // Calculate checksums for non-image files
            val fileChecksums = mutableMapOf<String, String>()
            fileChecksums[roomFile.name] = calculateSHA256(roomFile)
            fileChecksums["settings.json"] = calculateSHA256(settingsFile)

            // Calculate checksums for images separately
            val imageChecksums = imageFiles.map { imageFile ->
                ImageChecksum(
                    filename = imageFile.name,
                    checksum = calculateSHA256(imageFile)
                )
            }

            // Generate manifest
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val manifest = BackupManifest(
                app = AppInfo(
                    name = "Yearly Progress",
                    packageName = context.packageName,
                    versionName = pInfo.versionName ?: "unknown",
                    versionCode = pInfo.longVersionCode.toInt()
                ),
                backup = BackupInfo(
                    createdAt = Instant.now().toString(),
                    deviceSdk = Build.VERSION.SDK_INT,
                    files = BackupFiles(
                        database = true,
                        settings = true,
                        images = imageCount
                    )
                ),
                integrity = IntegrityInfo(
                    algorithm = "SHA-256",
                    fileChecksums = fileChecksums,
                    imageChecksums = imageChecksums
                )
            )

            val manifestFile = File(tempDir, "manifest.json").apply {
                writeText(json.encodeToString(BackupManifest.serializer(), manifest))
            }

            // Add manifest to entries
            zipEntries["manifest.json"] = manifestFile

            // Create ZIP
            val zipFile = File(context.cacheDir, "backup.ypp")
            ZipUtils.zipWithPaths(zipEntries, zipFile)

            // Calculate final ZIP checksum and log it
            val zipChecksum = calculateSHA256(zipFile)
            Log.d("BackupManager", "Backup created - ZIP SHA-256: $zipChecksum")

            // Copy to output URI
            context.contentResolver.openOutputStream(outputUri)?.use { out ->
                zipFile.inputStream().use { it.copyTo(out) }
            }

            tempDir.deleteRecursively()
            zipFile.delete()

        } catch (e: Exception) {
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

            // Log ZIP checksum for verification
            if (ZipUtils.isZipFile(backupFile)) {
                val zipChecksum = calculateSHA256(backupFile)
                Log.d("BackupManager", "Restoring backup - ZIP SHA-256: $zipChecksum")
            }

            // 2. Check if it's a zip (new format) or sqlite/aes (old format)
            if (ZipUtils.isZipFile(backupFile)) {
                // New format: .ypp (zip)
                val zipFile = File(tempDir, "backup.zip")
                backupFile.renameTo(zipFile)

                val extractedFiles = ZipUtils.unzip(zipFile, tempDir)

                // Validate manifest if present
                val manifestFile = extractedFiles.firstOrNull { it.name == "manifest.json" }
                if (manifestFile != null) {
                    validateManifest(manifestFile, extractedFiles)
                }

                val roomFile = extractedFiles.firstOrNull { it.name.endsWith(".aes") }
                val prefsFile = extractedFiles.firstOrNull { it.name == "settings.json" }

                // Restore database
                roomFile?.let { helper.restoreFromCustomFile(it) }

                // Restore App Settings
                prefsFile?.let {
                    val settings = json.decodeFromString(AppSettings.serializer(), it.readText())
                    appSettingsRepository.setAppSettings(settings)
                }

                // Restore images
                val imagesDir = context.filesDir
                extractedFiles
                    .filter { it.parentFile?.name == "images" || it.path.contains("/images/") }
                    .forEach { imageFile ->
                        val destFile = File(imagesDir, imageFile.name)
                        imageFile.copyTo(destFile, overwrite = true)
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

    private fun validateManifest(manifestFile: File, extractedFiles: List<File>) {
        try {
            val manifest = json.decodeFromString(BackupManifest.serializer(), manifestFile.readText())

            // Validate format version
            require(manifest.formatVersion == 1) {
                "Unsupported backup format version: ${manifest.formatVersion}. Please update the app."
            }

            // Validate package name
            require(manifest.app.packageName == context.packageName) {
                "Backup is from a different app (${manifest.app.packageName}). Cannot restore."
            }

            // Validate version code (backup shouldn't be from a newer version)
            val currentVersionCode = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .longVersionCode.toInt()

            require(manifest.app.versionCode <= currentVersionCode) {
                "Backup is from a newer app version (${manifest.app.versionCode}). Current version: $currentVersionCode. Please update the app first."
            }

            // Validate checksums for non-image files
            if (manifest.integrity.fileChecksums.isNotEmpty()) {
                validateFileChecksums(manifest.integrity.fileChecksums, extractedFiles)
            }

            // Validate image checksums
            if (manifest.integrity.imageChecksums.isNotEmpty()) {
                validateImageChecksums(manifest.integrity.imageChecksums, extractedFiles)
            }

            Log.d("BackupManager", "Manifest validated: v${manifest.app.versionName} (${manifest.app.versionCode}), created ${manifest.backup.createdAt}")

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            // If manifest parsing fails, log but don't fail the restore
            // This allows compatibility with backups that don't have manifests
            Log.w("BackupManager", "Failed to parse manifest, continuing restore", e)
        }
    }

    private fun validateFileChecksums(expectedChecksums: Map<String, String>, extractedFiles: List<File>) {
        var validatedCount = 0
        var mismatchCount = 0

        extractedFiles.forEach { file ->
            // Find the relative path in the ZIP structure (excluding images)
            val relativePath = when {
                file.name == "manifest.json" -> "manifest.json"
                file.name == "settings.json" -> "settings.json"
                file.name.endsWith(".aes") -> file.name
                file.parentFile?.name == "images" || file.path.contains("/images/") -> null // Skip images
                else -> file.name
            }

            if (relativePath != null) {
                val expectedChecksum = expectedChecksums[relativePath]
                if (expectedChecksum != null) {
                    val actualChecksum = calculateSHA256(file)
                    if (expectedChecksum == actualChecksum) {
                        validatedCount++
                        Log.d("BackupManager", "Checksum verified: $relativePath")
                    } else {
                        mismatchCount++
                        Log.w("BackupManager", "Checksum mismatch for $relativePath")
                        Log.w("BackupManager", "Expected: $expectedChecksum")
                        Log.w("BackupManager", "Actual: $actualChecksum")
                    }
                }
            }
        }

        if (mismatchCount > 0) {
            throw SecurityException("Backup integrity check failed: $mismatchCount file(s) have invalid checksums")
        }

        Log.d("BackupManager", "All file checksums validated successfully ($validatedCount files)")
    }

    private fun validateImageChecksums(imageChecksums: List<ImageChecksum>, extractedFiles: List<File>) {
        var validatedCount = 0
        var mismatchCount = 0

        // Get all image files from extracted files
        val imageFiles = extractedFiles.filter {
            it.parentFile?.name == "images" || it.path.contains("/images/")
        }

        imageChecksums.forEach { expectedImage ->
            val imageFile = imageFiles.firstOrNull { it.name == expectedImage.filename }

            if (imageFile != null) {
                val actualChecksum = calculateSHA256(imageFile)
                if (expectedImage.checksum == actualChecksum) {
                    validatedCount++
                    Log.d("BackupManager", "Image checksum verified: ${expectedImage.filename}")
                } else {
                    mismatchCount++
                    Log.w("BackupManager", "Image checksum mismatch for ${expectedImage.filename}")
                    Log.w("BackupManager", "Expected: ${expectedImage.checksum}")
                    Log.w("BackupManager", "Actual: $actualChecksum")
                }
            } else {
                mismatchCount++
                Log.w("BackupManager", "Image file not found: ${expectedImage.filename}")
            }
        }

        if (mismatchCount > 0) {
            throw SecurityException("Backup integrity check failed: $mismatchCount image(s) have invalid checksums or are missing")
        }

        Log.d("BackupManager", "All image checksums validated successfully ($validatedCount images)")
    }

    /**
     * Calculate SHA-256 checksum of a file
     */
    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
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