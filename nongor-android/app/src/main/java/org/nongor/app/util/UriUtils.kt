package org.nongor.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

/**
 * Decode an image URI, downscale it so its longest edge is at most [maxEdge] px, and write it to
 * cache as JPEG. Full-resolution camera photos are 10+ MB and would risk OOM / multi-minute stalls
 * when handed to on-device Gemma on the low-RAM phones this app targets; a ~1024px image is plenty
 * for scene triage. Returns null if the image can't be decoded.
 */
fun decodeDownscaledToCache(
    context: Context, uri: Uri, maxEdge: Int = 1024, quality: Int = 85, prefix: String = "img",
): File? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    while (longest / sample > maxEdge) sample *= 2   // inSampleSize must be a power of two
    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
    val decoded = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, decodeOpts)
    } ?: return null

    // The power-of-two sample may still leave the longest edge above maxEdge; scale the rest.
    val scale = maxEdge.toFloat() / maxOf(decoded.width, decoded.height)
    val bitmap = if (scale < 1f) {
        Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1), true)
            .also { if (it !== decoded) decoded.recycle() }
    } else decoded

    val out = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
    FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }
    bitmap.recycle()
    return out
}

fun copyUriToCacheFile(context: Context, uri: Uri, suffix: String? = null): File {
    val extension = suffix ?: inferFileExtension(context, uri)
    val out = File(context.cacheDir, "import_${System.currentTimeMillis()}.$extension")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(out).use { output -> input.copyTo(output) }
    } ?: error("Could not open $uri")
    return out
}

fun saveBitmapToCacheFile(context: Context, bitmap: Bitmap, suffix: String): File {
    val out = File(context.cacheDir, "capture_${System.currentTimeMillis()}.$suffix")
    FileOutputStream(out).use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
    }
    return out
}

private fun inferFileExtension(context: Context, uri: Uri): String {
    val mimeType = context.contentResolver.getType(uri)?.lowercase()
    val mimeExtension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
    if (!mimeExtension.isNullOrBlank()) return mimeExtension

    val pathExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        ?.lowercase()
        ?.substringBefore('?')
        ?.substringBefore('#')
        ?.takeIf { it.isNotBlank() }
    if (!pathExtension.isNullOrBlank()) return pathExtension

    return "jpg"
}
