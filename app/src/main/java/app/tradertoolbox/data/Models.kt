package app.tradertoolbox.data

import org.json.JSONArray
import org.json.JSONObject

data class Category(val id: String, val title: String)

data class Tool(
    val id: String,
    val name: String,
    val desc: String,
    val category: String,
    val markets: List<String>,
    val pricing: String,
    val access: String,
    val url: String,
    val tags: List<String>,
    val sponsored: Boolean,
    val featured: Boolean,
    val lang: String,
)

data class ToolsData(
    val version: Int,
    val updated: String,
    val categories: List<Category>,
    val tools: List<Tool>,
)

private fun JSONArray?.toStringList(): List<String> {
    val array = this ?: return emptyList()
    return List(array.length()) { i -> array.optString(i) }
}

/** فایل JSON را می‌خواند. فقط ابزارهایی که آدرس https دارند پذیرفته می‌شوند. */
fun parseToolsData(json: String): ToolsData {
    val root = JSONObject(json)
    val cats = root.getJSONArray("categories")
    val tools = root.getJSONArray("tools")

    val categories = List(cats.length()) { i ->
        val c = cats.getJSONObject(i)
        Category(id = c.getString("id"), title = c.getString("title"))
    }

    val toolList = List(tools.length()) { i ->
        val t = tools.getJSONObject(i)
        Tool(
            id = t.getString("id"),
            name = t.getString("name"),
            desc = t.optString("desc", ""),
            category = t.getString("category"),
            markets = t.optJSONArray("markets").toStringList(),
            pricing = t.optString("pricing", "free"),
            access = t.optString("access", "unknown"),
            url = t.getString("url"),
            tags = t.optJSONArray("tags").toStringList(),
            sponsored = t.optBoolean("sponsored", false),
            featured = t.optBoolean("featured", false),
            lang = t.optString("lang", "en"),
        )
    }.filter { it.url.startsWith("https://") }

    return ToolsData(
        version = root.optInt("version", 1),
        updated = root.optString("updated", ""),
        categories = categories,
        tools = toolList,
    )
}
