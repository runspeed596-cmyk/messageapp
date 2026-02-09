package com.Kelasor.app.data.remote.util

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File

/**
 * A custom RequestBody that tracks upload progress.
 * Used for files larger than 1MB to show a progress indicator.
 */
class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val listener: ProgressListener
) : RequestBody() {

    companion object {
        private const val SEGMENT_SIZE = 8192L // 8KB segments
    }

    interface ProgressListener {
        fun onProgressUpdate(bytesWritten: Long, totalBytes: Long)
    }

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val source = file.source()
        val total = file.length()
        var written: Long = 0

        source.use {
            var read: Long
            var lastProgress = -1
            while (it.read(sink.buffer, SEGMENT_SIZE).also { bytesRead -> read = bytesRead } != -1L) {
                written += read
                sink.flush()
                
                val currentProgress = ((written * 100) / total).toInt()
                if (currentProgress > lastProgress || written == total) {
                    lastProgress = currentProgress
                    listener.onProgressUpdate(written, total)
                }
            }
        }
    }
}
