package com.example.data.device

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class PickedDocumentInfo(
    val uriString: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val pageCount: Int?,
    val extractedText: String
)

object DocumentReaderHelper {
    suspend fun readDocument(context: Context, uri: Uri): PickedDocumentInfo = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var fileName = "Document"
        var sizeBytes = 0L

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) fileName = cursor.getString(nameIdx) ?: "Document"
                if (sizeIdx >= 0) sizeBytes = cursor.getLong(sizeIdx)
            }
        }

        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val isPdf = mimeType.contains("pdf", ignoreCase = true) ||
            fileName.endsWith(".pdf", ignoreCase = true)

        var pageCount: Int? = null
        val textPreview = StringBuilder()

        if (isPdf) {
            try {
                resolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        pageCount = renderer.pageCount
                    }
                }
            } catch (_: Exception) {
                pageCount = null
            }
            textPreview.append("PDF file loaded locally (${pageCount ?: "?"} pages). ")
            textPreview.append("Full semantic PDF OCR & deep Q&A requires Firebase AI Logic multimodal analysis.")
        } else {
            try {
                resolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                        var charsRead = 0
                        val maxChars = 6000
                        var line = reader.readLine()
                        while (line != null && charsRead < maxChars) {
                            textPreview.append(line).append('\n')
                            charsRead += line.length + 1
                            line = reader.readLine()
                        }
                    }
                }
            } catch (e: Exception) {
                textPreview.append("Unable to decode text stream: ${e.localizedMessage ?: "unknown format"}")
            }
        }

        PickedDocumentInfo(
            uriString = uri.toString(),
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            pageCount = pageCount,
            extractedText = textPreview.toString().trim()
        )
    }
}
