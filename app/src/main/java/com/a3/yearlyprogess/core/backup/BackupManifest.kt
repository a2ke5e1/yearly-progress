package com.a3.yearlyprogess.core.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupManifest(
    val formatVersion: Int = 1,
    val app: AppInfo,
    val backup: BackupInfo,
    val integrity: IntegrityInfo
)

@Serializable
data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int
)

@Serializable
data class BackupInfo(
    val createdAt: String,
    val deviceSdk: Int,
    val files: BackupFiles
)

@Serializable
data class BackupFiles(
    val database: Boolean,
    val settings: Boolean,
    val images: Int
)

@Serializable
data class IntegrityInfo(
    val algorithm: String,
    val generatedBy: String,
    val fileChecksums: Map<String, String> = emptyMap()
)