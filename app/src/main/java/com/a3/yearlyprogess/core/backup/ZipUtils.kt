package com.a3.yearlyprogess.core.backup

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {

    fun zip(files: List<File>, output: File) {
        ZipOutputStream(output.outputStream()).use { zip ->
            files.forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                file.inputStream().copyTo(zip)
                zip.closeEntry()
            }
        }
    }

    fun unzip(zipFile: File, outputDir: File): List<File> {
        val extracted = mutableListOf<File>()
        ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val outFile = File(outputDir, entry.name)
                outFile.outputStream().use { zip.copyTo(it) }
                extracted += outFile
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return extracted
    }

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
                    0x04034B50, // Local file header
                    0x02014B50, // Central directory
                    0x06054B50  // End of central directory (empty zip)
                        -> true
                    else -> false
                }
            }
        } catch (_: IOException) {
            false
        }
    }
}
