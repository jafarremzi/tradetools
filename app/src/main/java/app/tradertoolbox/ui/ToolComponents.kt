package app.tradertoolbox.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tradertoolbox.data.Tool

fun pricingLabel(pricing: String): String = when (pricing) {
    "free" -> "رایگان"
    "freemium" -> "رایگان + پولی"
    "paid" -> "پولی"
    else -> pricing
}

fun marketLabel(market: String): String = when (market) {
    "forex" -> "فارکس"
    "crypto" -> "کریپتو"
    else -> market
}

fun accessLabel(access: String): String = when (access) {
    "direct" -> "دسترسی مستقیم"
    "needs_vpn" -> "نیاز به تغییر IP"
    else -> "بررسی نشده"
}

fun openUrl(context: Context, url: String) {
    if (!url.startsWith("https://")) return
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "مرورگری برای باز کردن لینک پیدا نشد", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun Tag(text: String, accent: Boolean = false) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (accent) Palette.HeroBg else Color.Transparent)
            .border(1.dp, if (accent) Palette.HeroBorder else Palette.BorderStrong, shape)
            .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) Palette.Accent else Palette.TextSoft,
        )
    }
}

private val tilePalette = listOf(
    Color(0xFF16304D) to Color(0xFF8EC5FF),
    Color(0xFF2A2350) to Color(0xFFB9A8FF),
    Color(0xFF3A2E14) to Color(0xFFFFD27A),
    Color(0xFF16352A) to Color(0xFF86EFAC),
    Color(0xFF3A1E26) to Color(0xFFFDA4AF),
)

private fun tileColors(category: String): Pair<Color, Color> =
    tilePalette[(category.hashCode() and 0x7fffffff) % tilePalette.size]

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolCard(
    tool: Tool,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (tileBg, tileFg) = tileColors(tool.category)
    val letter = tool.name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"

    AppCard(modifier = modifier, onClick = onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tileBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(letter, color = tileFg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "حذف از علاقه‌مندی‌ها" else "افزودن به علاقه‌مندی‌ها",
                            tint = if (isFavorite) Palette.Accent else Palette.Muted,
                        )
                    }
                }
                Text(tool.desc, style = MaterialTheme.typography.bodyMedium, color = Palette.TextSoft)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (tool.sponsored) Tag("حمایت‌شده", accent = true)
                    Tag(pricingLabel(tool.pricing))
                    if (tool.markets.isNotEmpty()) {
                        Tag(tool.markets.joinToString(" · ") { marketLabel(it) })
                    }
                    if (tool.lang == "fa") Tag("فارسی", accent = true)
                    if (tool.access == "direct" || tool.access == "needs_vpn") Tag(accessLabel(tool.access))
                }
            }
        }
    }
}

@Composable
fun ToolDetailDialog(tool: Tool, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val host = try {
        Uri.parse(tool.url).host ?: tool.url
    } catch (e: Exception) {
        tool.url
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tool.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tool.desc)
                Text("نوع: ${pricingLabel(tool.pricing)}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "وضعیت دسترسی از ایران: ${accessLabel(tool.access)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text("\u200E$host", style = MaterialTheme.typography.bodyMedium)
                if (tool.sponsored) {
                    Text(
                        "این مورد تبلیغاتی (حمایت‌شده) است.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    "لینک در مرورگر گوشی باز می‌شود. محتوا و خدمات این وب‌سایت متعلق به شخص ثالث است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { openUrl(context, tool.url) }) { Text("باز کردن سایت") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        },
    )
}
