package com.a3.yearlyprogess.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

fun resizeImageForAppStorage(context: Context, uri: Uri): Bitmap {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    }

    // Target 1920px (Full HD) - Looks sharp in App, manageable file size
    val maxSize = 1920
    var scale = 1
    while (options.outWidth / scale > maxSize || options.outHeight / scale > maxSize) {
        scale *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = scale
        // ARGB_8888 is default, gives full vibrant colors
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, decodeOptions)
    } ?: throw IllegalStateException("Failed to decode")
}

fun loadBitmapOptimizedForWidget(context: Context, path: String): Bitmap? {
    val file = File(path)
    if (!file.exists()) return null

    // 1. Check dimensions
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, options)

    // 2. Calculate scale for Widget limits (Target 720px)
    val maxSize = 720
    var inSampleSize = 1
    while (options.outHeight / inSampleSize >= maxSize && options.outWidth / inSampleSize >= maxSize) {
        inSampleSize *= 2
    }

    // 3. Decode with RGB_565 (The secret to fixing crashes)
    val decodeOptions = BitmapFactory.Options().apply {
        this.inSampleSize = inSampleSize
        this.inPreferredConfig = Bitmap.Config.RGB_565 // Saves 50% RAM
    }

    val roughBitmap = BitmapFactory.decodeFile(path, decodeOptions) ?: return null

    // 4. Final Exact Scale (Filter=true makes it smooth, not pixelated)
    val ratio = roughBitmap.width.toFloat() / roughBitmap.height.toFloat()
    val finalWidth: Int
    val finalHeight: Int

    if (roughBitmap.width > roughBitmap.height) {
        finalWidth = if (roughBitmap.width > maxSize) maxSize else roughBitmap.width
        finalHeight = (finalWidth / ratio).toInt()
    } else {
        finalHeight = if (roughBitmap.height > maxSize) maxSize else roughBitmap.height
        finalWidth = (finalHeight * ratio).toInt()
    }

    // If the rough decode was already close enough, return it to save CPU
    if (roughBitmap.width <= maxSize && roughBitmap.height <= maxSize) return roughBitmap

    return try {
        val scaled = Bitmap.createScaledBitmap(roughBitmap, finalWidth, finalHeight, true)
        if (scaled != roughBitmap) roughBitmap.recycle()
        scaled
    } catch (e: OutOfMemoryError) {
        roughBitmap // Fallback to the rough one if scaling fails
    }
}