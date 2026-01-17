package com.a3.yearlyprogess.core.backup

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {

    /**
     * Create a ZIP file from a list of files.
     * All files are placed at the root of the ZIP.
     */
    fun zip(files: List<File>, output: File) {
        ZipOutputStream(output.outputStream()).use { zip ->
            files.forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                file.inputStream().copyTo(zip)
                zip.closeEntry()
            }
        }
    }

    /**
     * Create a ZIP file with custom paths for each entry.
     * This allows organizing files into subdirectories within the ZIP.
     *
     * @param entries Map of ZIP entry path to source file
     * @param output The ZIP file to create
     */
    fun zipWithPaths(entries: Map<String, File>, output: File) {
        ZipOutputStream(output.outputStream()).use { zip ->
            entries.forEach { (entryName, file) ->
                // Create ZIP entry with the custom path
                val entry = ZipEntry(entryName)
                zip.putNextEntry(entry)
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /**
     * Extract all files from a ZIP archive.
     * Preserves the directory structure from the ZIP.
     *
     * @param zipFile The ZIP file to extract
     * @param outputDir The directory to extract files to
     * @return List of extracted files
     */
    fun unzip(zipFile: File, outputDir: File): List<File> {
        val extracted = mutableListOf<File>()
        ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                // Create output file respecting the entry's path
                val outFile = File(outputDir, entry.name)

                // Create parent directories if needed
                outFile.parentFile?.mkdirs()

                // Extract file
                outFile.outputStream().use { zip.copyTo(it) }
                extracted += outFile

                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return extracted
    }

    /**
     * Check if a file is a valid ZIP archive by examining its magic bytes.
     *
     * @param file The file to check
     * @return true if the file is a ZIP archive, false otherwise
     */
    fun isZipFile(file: File): Boolean {
        if (!file.exists() || file.isDirectory || !file.canRead() || file.length() < 4) {
            return false
        }

        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(4)
                if (fis.read(header) != 4) return false

                val signature = ByteBuffer.wrap(header)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .int

                when (signature) {
                    0x04034B50, // Local file header signature (PK\003\004)
                    0x02014B50, // Central directory file header (PK\001\002)
                    0x06054B50  // End of central directory record (PK\005\006 - empty zip)
                        -> true
                    else -> false
                }
            }
        } catch (_: IOException) {
            false
        }
    }

    /**
     * Get a list of entries in a ZIP file without extracting.
     * Useful for validation or preview.
     *
     * @param zipFile The ZIP file to inspect
     * @return List of entry names in the ZIP
     */
    fun listEntries(zipFile: File): List<String> {
        val entries = mutableListOf<String>()
        ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entries += entry.name
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return entries
    }
}