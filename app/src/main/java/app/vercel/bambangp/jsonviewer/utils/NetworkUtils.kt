package app.vercel.bambangp.jsonviewer.utils

import android.webkit.URLUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException

object NetworkUtils {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun loadJsonFromUrl(url: String): String {
        return try {
            if (!URLUtil.isValidUrl(url)) {
                throw IllegalArgumentException("Invalid URL format")
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.string() ?: throw IOException("Empty response body")
        } catch (e: Exception) {
            throw when (e) {
                is UnknownHostException -> IOException("No internet connection")
                is SocketTimeoutException -> IOException("Connection timeout")
                is SSLHandshakeException -> IOException("Security error")
                else -> e
            }
        }
    }
}