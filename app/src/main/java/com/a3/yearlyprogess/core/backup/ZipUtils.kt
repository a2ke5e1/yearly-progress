package com.a3.yearlyprogess.core.backup

import java.io.File
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
}
