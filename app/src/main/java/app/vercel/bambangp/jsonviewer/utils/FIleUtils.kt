package app.vercel.bambangp.jsonviewer.utils

import java.io.File

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.InputStreamReader

object FileUtils {

    /**
     * Safely reads text content from a URI
     */
    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    stringBuilder.toString()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts a human-readable filename from URI
     */
    fun getFileName(context: Context, uri: Uri): String {
        return when (uri.scheme) {
            "content" -> DocumentFile.fromSingleUri(context, uri)?.name
                ?: "file_${System.currentTimeMillis()}.json"
            "file" -> uri.lastPathSegment ?: "unknown.json"
            else -> "download_${System.currentTimeMillis()}.json"
        }
    }

    /**
     * Validates JSON file extension
     */
    fun isJsonFile(uri: Uri): Boolean {
        return when (uri.scheme) {
            "content" -> uri.toString().contains("json", ignoreCase = true)
            "file" -> uri.path?.endsWith(".json", ignoreCase = true) ?: false
            else -> false
        }
    }

    /**
     * Creates a temporary cache file
     */
    fun createTempJsonFile(context: Context, content: String): Uri {
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.json")
        file.writeText(content)
        return Uri.fromFile(file)
    }
}