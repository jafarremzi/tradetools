package app.tradertoolbox

import app.tradertoolbox.data.Tool
import app.tradertoolbox.logic.MarketFilter
import app.tradertoolbox.logic.filterTools
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolFilterTest {

    private fun tool(
        id: String,
        name: String,
        markets: List<String>,
        lang: String = "en",
        category: String = "charts",
        featured: Boolean = false,
        tags: List<String> = emptyList(),
    ) = Tool(
        id = id, name = name, desc = "توضیح $name", category = category,
        markets = markets, pricing = "free", access = "unknown",
        url = "https://example.com/$id", tags = tags,
        sponsored = false, featured = featured, lang = lang,
    )

    private val sample = listOf(
        tool("a", "Alpha", listOf("forex")),
        tool("b", "Beta", listOf("crypto"), featured = true),
        tool("c", "قیمت تتر", listOf("crypto"), lang = "fa", category = "iran_market", tags = listOf("تتر")),
    )

    @Test
    fun marketFilter_forex() {
        val r = filterTools(sample, "", MarketFilter.FOREX, false, null)
        assertEquals(listOf("a"), r.map { it.id })
    }

    @Test
    fun onlyPersian() {
        val r = filterTools(sample, "", MarketFilter.ALL, true, null)
        assertEquals(listOf("c"), r.map { it.id })
    }

    @Test
    fun categoryFilter() {
        val r = filterTools(sample, "", MarketFilter.ALL, false, "iran_market")
        assertEquals(listOf("c"), r.map { it.id })
    }

    @Test
    fun query_matchesNameAndTags_caseInsensitive() {
        assertEquals(listOf("a"), filterTools(sample, "ALPHA", MarketFilter.ALL, false, null).map { it.id })
        assertEquals(listOf("c"), filterTools(sample, "تتر", MarketFilter.ALL, false, null).map { it.id })
    }

    @Test
    fun featuredComesFirst() {
        val r = filterTools(sample, "", MarketFilter.ALL, false, null)
        assertEquals("b", r.first().id)
    }
}
