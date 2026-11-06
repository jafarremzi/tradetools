package app.tradertoolbox.logic

import app.tradertoolbox.data.Tool

enum class MarketFilter(val label: String) {
    ALL("همه بازارها"),
    FOREX("فارکس"),
    CRYPTO("کریپتو"),
}

/** حروف عربی را به فارسی یکسان می‌کند تا جستجو دقیق‌تر شود. */
fun normalizeFa(text: String): String =
    text.replace('ي', 'ی').replace('ك', 'ک').trim().lowercase()

fun filterTools(
    tools: List<Tool>,
    query: String,
    market: MarketFilter,
    onlyPersian: Boolean,
    category: String?,
): List<Tool> {
    val q = normalizeFa(query)
    return tools
        .filter { tool ->
            val marketOk = when (market) {
                MarketFilter.ALL -> true
                MarketFilter.FOREX -> "forex" in tool.markets
                MarketFilter.CRYPTO -> "crypto" in tool.markets
            }
            val langOk = !onlyPersian || tool.lang == "fa"
            val categoryOk = category == null || tool.category == category
            val queryOk = q.isEmpty() ||
                normalizeFa(tool.name).contains(q) ||
                normalizeFa(tool.desc).contains(q) ||
                tool.tags.any { normalizeFa(it).contains(q) }
            marketOk && langOk && categoryOk && queryOk
        }
        .sortedByDescending { it.featured }
}
