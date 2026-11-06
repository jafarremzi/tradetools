package app.tradertoolbox.data

import android.content.Context
import app.tradertoolbox.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ToolsRepository(private val context: Context) {

    private val cacheFile: File
        get() = File(context.filesDir, "tools_cache.json")

    /** فهرست داخل اپ و نسخه ذخیره‌شده را می‌خواند و جدیدتر را برمی‌گرداند. */
    suspend fun load(): ToolsData = withContext(Dispatchers.IO) {
        val bundledText = context.assets.open("tools.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val bundled = parseToolsData(bundledText)
        val cached = try {
            if (cacheFile.exists()) parseToolsData(cacheFile.readText(Charsets.UTF_8)) else null
        } catch (e: Exception) {
            null
        }
        if (cached != null && cached.updated >= bundled.updated) cached else bundled
    }

    /** اگر آدرس سرور تنظیم شده و فهرست جدیدتری وجود داشته باشد، ذخیره می‌کند و true برمی‌گرداند. */
    suspend fun refreshFromRemote(): Boolean = withContext(Dispatchers.IO) {
        val url = Config.REMOTE_TOOLS_URL
        if (!url.startsWith("https://")) return@withContext false
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.requestMethod = "GET"
            try {
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext false
                val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val parsed = parseToolsData(text)
                if (parsed.tools.isEmpty()) return@withContext false
                val current = load()
                if (parsed.updated > current.updated) {
                    cacheFile.writeText(text, Charsets.UTF_8)
                    true
                } else {
                    false
                }
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            false
        }
    }
}
