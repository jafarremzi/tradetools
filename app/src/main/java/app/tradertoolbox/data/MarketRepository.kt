package app.tradertoolbox.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * دریافت داده از چند API عمومی و بدون کلید.
 * فقط آدرس‌های ثابت و https استفاده می‌شود و هیچ ورودی کاربر وارد آدرس نمی‌شود.
 */
class MarketRepository {

    private fun httpGet(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "TraderToolbox/0.2 (Android)")
        try {
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("HTTP $code")
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    /** شاخص ترس و طمع کریپتو (Alternative.me). */
    suspend fun fetchFearGreed(): FearGreed = withContext(Dispatchers.IO) {
        val root = JSONObject(httpGet("https://api.alternative.me/fng/?limit=1&format=json"))
        val item = root.getJSONArray("data").getJSONObject(0)
        val value = item.get("value").toString().toDouble().toInt()
        FearGreed(value = value, label = item.optString("value_classification", ""))
    }

    /** قیمت بیت‌کوین (mempool.space). */
    suspend fun fetchBtcPrice(): BtcPrice = withContext(Dispatchers.IO) {
        val o = JSONObject(httpGet("https://mempool.space/api/v1/prices"))
        val usd = if (o.has("USD")) o.optDouble("USD") else null
        val eur = if (o.has("EUR")) o.optDouble("EUR") else null
        if (usd == null && eur == null) throw IOException("empty price")
        BtcPrice(usd = usd, eur = eur)
    }

    /** کارمزد پیشنهادی شبکه بیت‌کوین بر حسب sat/vB (mempool.space). */
    suspend fun fetchBtcFees(): BtcFees = withContext(Dispatchers.IO) {
        val o = JSONObject(httpGet("https://mempool.space/api/v1/fees/recommended"))
        BtcFees(
            fastest = o.getDouble("fastestFee"),
            halfHour = o.getDouble("halfHourFee"),
            hour = o.getDouble("hourFee"),
            economy = o.getDouble("economyFee"),
        )
    }

    /** TVL زنجیره‌ها (DefiLlama). */
    suspend fun fetchChains(): List<ChainTvl> = withContext(Dispatchers.IO) {
        val arr = JSONArray(httpGet("https://api.llama.fi/v2/chains"))
        val list = ArrayList<ChainTvl>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.optString("name", "")
            if (name.isNotEmpty()) list.add(ChainTvl(name, o.optDouble("tvl", 0.0)))
        }
        if (list.isEmpty()) throw IOException("empty chains")
        list
    }

    /** نرخ مرجع روزانه بانک مرکزی اروپا از طریق Frankfurter؛ زنده نیست. */
    suspend fun fetchEcbRates(): EcbRates = withContext(Dispatchers.IO) {
        val wanted = listOf("EUR", "GBP", "JPY", "CHF", "AUD", "CAD")
        val url = "https://api.frankfurter.dev/v1/latest?from=USD&to=" + wanted.joinToString(",")
        val root = JSONObject(httpGet(url))
        val rates = root.getJSONObject("rates")
        val quotes = wanted.filter { rates.has(it) }.map { RateQuote(it, rates.getDouble(it)) }
        if (quotes.isEmpty()) throw IOException("empty rates")
        EcbRates(
            date = root.optString("date", ""),
            base = root.optString("base", "USD"),
            quotes = quotes,
        )
    }
}
