package app.tradertoolbox.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** فاصله داخلی صفحه‌ها؛ پایین زیاد است تا زیر نوار شناور نرود. */
val ScreenPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(
        containerColor = if (hero) Palette.HeroBg else Palette.Surface,
        contentColor = Palette.Text,
    )
    val border = BorderStroke(1.dp, if (hero) Palette.HeroBorder else Palette.Border)
    val shape = RoundedCornerShape(20.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, border = border) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, border = border) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

/** چیپ قرصی برای فیلترها. */
@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) Palette.Accent else Palette.Surface)
            .border(1.dp, if (selected) Palette.Accent else Palette.BorderStrong, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) Palette.OnAccent else Palette.TextSoft,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

/** کنترل چندحالته (مثل «فارکس / کریپتو»). */
@Composable
fun Segmented(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Palette.Surface)
            .border(1.dp, Palette.Border, shape)
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(shape)
                    .background(if (selected) Palette.Raised else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (selected) Color.White else Palette.TextSoft,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Palette.Surface)
            .border(1.dp, Palette.Border, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** لوگوی ساده: سه کندل روی مربع گرد. */
@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.Accent),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val w = size.width
            val h = size.height
            val stroke = 2.5.dp.toPx()
            val ink = Palette.OnAccent
            drawLine(ink, Offset(w * 0.2f, h * 0.3f), Offset(w * 0.2f, h * 0.9f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.7f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.8f, h * 0.4f), Offset(w * 0.8f, h * 0.9f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

/** عدد درشت نتیجه با برچسب و واحد. */
@Composable
fun BigNumber(label: String, value: String, unit: String = "", color: Color = Palette.Accent) {
    Text(label, style = MaterialTheme.typography.labelLarge, color = Palette.Muted)
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("\u200E$value", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = color)
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium,
                color = Palette.Text,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
    }
}
