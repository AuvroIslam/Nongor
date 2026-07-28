package org.nongor.app.data.download

import android.content.Context
import org.nongor.app.inference.ModelPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Direct download of the public `.litertlm` file from Hugging Face (same idea as AI Edge Gallery:
 * fetch the artifact URL; no app-level auth — same as opening the resolve URL in a browser).
 */
class HfDownloadRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .followRedirects(true)
        .build()

    suspend fun download(
        url: String,
        destFile: File,
        onProgress: (downloaded: Long, total: Long) -> Unit,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            destFile.parentFile?.mkdirs()
            val existing = if (destFile.exists()) destFile.length() else 0L

            val requestBuilder = Request.Builder()
                .url(url)
                .header("Accept", "*/*")

            if (existing > 0) {
                requestBuilder.header("Range", "bytes=$existing-")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body ?: return@withContext Result.failure(IOException("Empty body"))
                when (response.code) {
                    200 -> {
                        if (existing > 0) destFile.delete()
                        val total = body.contentLength()
                        val written = streamToFile(total, body.byteStream(), destFile, 0L, onProgress)
                        verifyComplete(written, total)
                    }
                    206 -> {
                        val contentRange = response.header("Content-Range")
                        val totalBytes = parseTotalFromContentRange(contentRange) ?: (existing + body.contentLength())
                        val written = streamToFile(totalBytes, body.byteStream(), destFile, existing, onProgress)
                        verifyComplete(written, totalBytes)
                    }
                    401, 403 -> return@withContext Result.failure(
                        IOException(
                            "HTTP ${response.code}: could not download the model file. " +
                                "Check network or that the file is publicly available at this URL.",
                        ),
                    )
                    else -> return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}"),
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseTotalFromContentRange(header: String?): Long? {
        if (header == null) return null
        val slash = header.lastIndexOf('/')
        if (slash <= 0 || slash >= header.length - 1) return null
        return header.substring(slash + 1).toLongOrNull()
    }

    /** Streams the body to disk and returns the total bytes now on disk (offset + newly written). */
    private fun streamToFile(
        totalKnown: Long,
        input: java.io.InputStream,
        destFile: File,
        initialOffset: Long,
        onProgress: (Long, Long) -> Unit,
    ): Long {
        val buffer = ByteArray(8192)
        var written = initialOffset
        val out = if (initialOffset > 0) java.io.FileOutputStream(destFile, true) else java.io.FileOutputStream(destFile)
        out.use { o ->
            input.use { i ->
                while (true) {
                    val r = i.read(buffer)
                    if (r == -1) break
                    o.write(buffer, 0, r)
                    written += r
                    val total = if (totalKnown >= 0) totalKnown else written
                    onProgress(written, total)
                }
            }
        }
        return written
    }

    /**
     * A dropped connection can end the stream early with no exception, leaving a truncated ~2.5 GB
     * model that would then load as garbage. Reject a short file so the caller surfaces an error;
     * the partial bytes stay on disk so a retry resumes via Range instead of restarting.
     */
    private fun verifyComplete(written: Long, expectedTotal: Long) {
        if (expectedTotal > 0 && written != expectedTotal) {
            throw IOException(
                "Incomplete download: received $written of $expectedTotal bytes. Retry to resume.")
        }
    }

    companion object {
        fun modelFile(context: Context): File =
            File(context.filesDir, "models/${ModelPaths.MODEL_FILENAME}")
    }
}
