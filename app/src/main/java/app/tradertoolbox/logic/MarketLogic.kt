package app.tradertoolbox.logic

import app.tradertoolbox.data.ChainTvl

fun topChains(chains: List<ChainTvl>, n: Int): List<ChainTvl> =
    chains.filter { it.tvl > 0.0 }.sortedByDescending { it.tvl }.take(n)

fun formatUsdCompact(value: Double): String = when {
    value >= 1e12 -> "\$" + formatNumber(value / 1e12, 2) + "T"
    value >= 1e9 -> "\$" + formatNumber(value / 1e9, 2) + "B"
    value >= 1e6 -> "\$" + formatNumber(value / 1e6, 2) + "M"
    else -> "\$" + formatNumber(value, 0)
}

fun fearGreedLabelFa(label: String): String = when (label.trim().lowercase()) {
    "extreme fear" -> "ترس شدید"
    "fear" -> "ترس"
    "neutral" -> "خنثی"
    "greed" -> "طمع"
    "extreme greed" -> "طمع شدید"
    else -> label
}

/** فاصله حداقلی بین دو دریافت: ۵ دقیقه برای خودکار و ۳۰ ثانیه برای دکمه دستی. */
fun shouldRefresh(nowMillis: Long, lastMillis: Long, force: Boolean): Boolean {
    if (lastMillis == 0L) return true
    val gap = if (force) 30_000L else 300_000L
    return nowMillis - lastMillis >= gap
}

val ecbCurrencyNamesFa: Map<String, String> = mapOf(
    "EUR" to "یورو",
    "GBP" to "پوند",
    "JPY" to "ین",
    "CHF" to "فرانک سوئیس",
    "AUD" to "دلار استرالیا",
    "CAD" to "دلار کانادا",
)
