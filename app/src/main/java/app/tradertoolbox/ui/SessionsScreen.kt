package app.tradertoolbox.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tradertoolbox.logic.SessionStatus
import app.tradertoolbox.logic.TimelineSegment
import app.tradertoolbox.logic.marketSessions
import app.tradertoolbox.logic.sessionSegments
import app.tradertoolbox.logic.sessionStatus
import app.tradertoolbox.logic.tehranDayFraction
import app.tradertoolbox.logic.tehranTime
import kotlinx.coroutines.delay
import java.time.Instant

@Composable
fun SessionsScreen() {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = Instant.now()
        }
    }
    val statuses = marketSessions.map { sessionStatus(it, now) }
    val nowFraction = tehranDayFraction(now)
    val londonOpen = statuses.firstOrNull { it.session.name == "لندن" }?.isOpen == true
    val newYorkOpen = statuses.firstOrNull { it.session.name == "نیویورک" }?.isOpen == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard(hero = true) {
                Text("ساعت تهران", style = MaterialTheme.typography.labelLarge, color = Palette.Muted)
                Text(
                    text = "\u200E${tehranTime(now)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Accent,
                )
            }
        }
        if (londonOpen && newYorkOpen) {
            item {
                Text(
                    "هم‌اکنون بازارهای لندن و نیویورک هم‌زمان باز هستند (همپوشانی دو سشن).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.Accent,
                )
            }
        }
        items(statuses, key = { it.session.name }) { status ->
            SessionCard(status, sessionSegments(status.session, now), nowFraction)
        }
        item {
            Text(
                "محور زمان ۲۴ ساعته و به وقت تهران است (از نیمه‌شب تا نیمه‌شب)؛ خط سفید زمان اکنون را نشان می‌دهد. ساعت‌ها بر اساس ساعت کاری رایج هر بازار و تقریبی هستند. روزهای شنبه و یکشنبه (به وقت محلی هر شهر) بسته در نظر گرفته شده و تعطیلات رسمی لحاظ نشده است.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
        }
    }
}

@Composable
private fun SessionCard(status: SessionStatus, segments: List<TimelineSegment>, nowFraction: Float) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(status.session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "ساعت محلی \u200E${status.localTime} · کاری \u200E${status.session.openHour}:00-${status.session.closeHour}:00",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.Muted,
                )
            }
            StatusPill(status.isOpen)
        }
        SessionTimeline(segments, nowFraction, status.isOpen)
    }
}

@Composable
private fun StatusPill(open: Boolean) {
    val shape = RoundedCornerShape(50)
    Text(
        text = if (open) "باز" else "بسته",
        color = if (open) Palette.Accent else Palette.TextSoft,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(shape)
            .background(if (open) Palette.HeroBg else Palette.Raised)
            .border(1.dp, if (open) Palette.HeroBorder else Palette.BorderStrong, shape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun SessionTimeline(segments: List<TimelineSegment>, nowFraction: Float, active: Boolean) {
    val segmentColor = if (active) Palette.Accent else Color(0xFF3A4B5A)
    Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
        val barHeight = 6.dp.toPx()
        val top = (size.height - barHeight) / 2f
        val radius = CornerRadius(barHeight / 2f)
        drawRoundRect(
            color = Palette.Track,
            topLeft = Offset(0f, top),
            size = Size(size.width, barHeight),
            cornerRadius = radius,
        )
        segments.forEach { s ->
            drawRoundRect(
                color = segmentColor,
                topLeft = Offset(s.start * size.width, top),
                size = Size((s.end - s.start) * size.width, barHeight),
                cornerRadius = radius,
            )
        }
        val x = nowFraction * size.width
        drawLine(
            color = Color.White,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}
