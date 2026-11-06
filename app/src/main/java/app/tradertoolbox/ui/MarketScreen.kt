package app.tradertoolbox.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tradertoolbox.data.BtcFees
import app.tradertoolbox.data.BtcPrice
import app.tradertoolbox.data.ChainTvl
import app.tradertoolbox.data.EcbRates
import app.tradertoolbox.data.FearGreed
import app.tradertoolbox.data.MarketState
import app.tradertoolbox.data.Section
import app.tradertoolbox.logic.ecbCurrencyNamesFa
import app.tradertoolbox.logic.fearGreedLabelFa
import app.tradertoolbox.logic.formatNumber
import app.tradertoolbox.logic.formatUsdCompact
import app.tradertoolbox.logic.topChains
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MarketScreen(vm: MarketViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.refresh(force = false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MarketHeader(
                state = state,
                onRefresh = {
                    if (!vm.refresh(force = true)) {
                        Toast.makeText(context, "چند لحظه بعد دوباره تلاش کنید", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
        item {
            SectionCard("شاخص ترس و طمع کریپتو", "Alternative.me", state.fearGreed, hero = true) {
                FearGreedContent(it)
            }
        }
        item {
            SectionCard("قیمت بیت‌کوین", "mempool.space", state.btcPrice) { BtcPriceContent(it) }
        }
        item {
            SectionCard("کارمزد شبکه بیت‌کوین", "mempool.space", state.btcFees) { BtcFeesContent(it) }
        }
        item {
            SectionCard("برترین زنجیره‌ها (TVL دیفای)", "DefiLlama", state.chains) { ChainsContent(it) }
        }
        item {
            SectionCard("نرخ مرجع ارزها (بانک مرکزی اروپا)", "Frankfurter / ECB", state.ecb) { EcbContent(it) }
        }
        item {
            Text(
                text = "داده‌ها از سرویس‌های عمومی جهانی گرفته می‌شود و ممکن است با تأخیر باشد یا از شبکه شما در دسترس نباشد (مثلاً نیاز به تغییر IP). این اطلاعات توصیه معاملاتی نیست.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
        }
    }
}

@Composable
private fun MarketHeader(state: MarketState, onRefresh: () -> Unit) {
    val status = when {
        state.refreshing -> "در حال به‌روزرسانی..."
        state.lastUpdatedMillis != 0L ->
            "آخرین به‌روزرسانی \u200E" + SimpleDateFormat("HH:mm", Locale.US).format(Date(state.lastUpdatedMillis))
        else -> "هنوز داده‌ای دریافت نشده است."
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("بازار امروز", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(status, style = MaterialTheme.typography.bodySmall, color = Palette.Muted)
        }
        CircleIconButton(onClick = onRefresh, contentDescription = "به‌روزرسانی", enabled = !state.refreshing) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Palette.Text)
        }
    }
}

@Composable
private fun <T> SectionCard(
    title: String,
    source: String,
    section: Section<T>,
    hero: Boolean = false,
    content: @Composable (T) -> Unit,
) {
    AppCard(hero = hero) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        when (section) {
            is Section.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Palette.Accent)
            is Section.Failed -> Text(
                section.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            is Section.Ready -> content(section.value)
        }
        Text("منبع: $source", style = MaterialTheme.typography.labelSmall, color = Palette.Muted)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Palette.TextSoft)
        Text("\u200E$value", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FearGreedContent(fg: FearGreed) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("\u200E${fg.value}", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Palette.Accent)
        Text(fearGreedLabelFa(fg.label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    FearGreedGauge(fg.value)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("ترس شدید", style = MaterialTheme.typography.labelSmall, color = Palette.Muted)
        Text("طمع شدید", style = MaterialTheme.typography.labelSmall, color = Palette.Muted)
    }
    Text(
        "این شاخص فقط سنجش احساسات بازار است و سیگنال خرید یا فروش نیست.",
        style = MaterialTheme.typography.bodySmall,
        color = Palette.Muted,
    )
}

/** نوار رنگی؛ صفر (ترس شدید) سمت راست و صد (طمع شدید) سمت چپ است. */
@Composable
private fun FearGreedGauge(value: Int) {
    val fromLeft = 1f - value.coerceIn(0, 100) / 100f
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(18.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.horizontalGradient(listOf(Palette.Good, Palette.Warn, Palette.Danger))),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = maxWidth * fromLeft - 9.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Palette.Bg, CircleShape),
            )
        }
    }
}

@Composable
private fun RowScope.PriceTile(label: String, value: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Raised)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Palette.Muted)
        Text("\u200E$value", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BtcPriceContent(price: BtcPrice) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        price.usd?.let { PriceTile("به دلار", "\$" + formatNumber(it, 0)) }
        price.eur?.let { PriceTile("به یورو", "€" + formatNumber(it, 0)) }
    }
}

@Composable
private fun RowScope.FeeCell(label: String, value: Double, accent: Boolean = false) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatNumber(value, 1),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (accent) Palette.Accent else Palette.Text,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Palette.Muted)
    }
}

@Composable
private fun BtcFeesContent(fees: BtcFees) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FeeCell("سریع", fees.fastest, accent = true)
        FeeCell("۳۰ دقیقه", fees.halfHour)
        FeeCell("۱ ساعت", fees.hour)
        FeeCell("کم‌اولویت", fees.economy)
    }
    Text("واحد: ‎sat/vB", style = MaterialTheme.typography.labelSmall, color = Palette.Muted)
}

@Composable
private fun ChainsContent(chains: List<ChainTvl>) {
    val top = topChains(chains, 5)
    val max = top.firstOrNull()?.tvl ?: 1.0
    top.forEachIndexed { index, chain ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InfoRow("${index + 1}. ${chain.name}", formatUsdCompact(chain.tvl))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Palette.Track),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (chain.tvl / max).toFloat().coerceIn(0.02f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Palette.Accent),
                )
            }
        }
    }
}

@Composable
private fun EcbContent(rates: EcbRates) {
    if (rates.date.isNotEmpty()) {
        Text("تاریخ نرخ: \u200E${rates.date}", style = MaterialTheme.typography.bodySmall, color = Palette.Muted)
    }
    rates.quotes.forEach { q ->
        val name = ecbCurrencyNamesFa[q.code] ?: q.code
        InfoRow("۱ دلار آمریکا =", formatNumber(q.rate, 4) + " " + name)
    }
    Text(
        "نرخ مرجع روزانه است، زنده نیست و برای معامله مناسب نیست.",
        style = MaterialTheme.typography.bodySmall,
        color = Palette.Muted,
    )
}
