package app.tradertoolbox.logic

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** ساعت کاری رایج هر بازار به وقت محلی خودش (تقریبی و بدون در نظر گرفتن تعطیلات رسمی). */
data class MarketSession(
    val name: String,
    val zoneId: String,
    val openHour: Int,
    val closeHour: Int,
)

data class SessionStatus(
    val session: MarketSession,
    val isOpen: Boolean,
    val localTime: String,
)

val marketSessions: List<MarketSession> = listOf(
    MarketSession("سیدنی", "Australia/Sydney", 7, 16),
    MarketSession("توکیو", "Asia/Tokyo", 9, 18),
    MarketSession("لندن", "Europe/London", 8, 17),
    MarketSession("نیویورک", "America/New_York", 8, 17),
)

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

fun sessionStatus(session: MarketSession, now: Instant): SessionStatus {
    val local = now.atZone(ZoneId.of(session.zoneId))
    val isWeekday = local.dayOfWeek != DayOfWeek.SATURDAY && local.dayOfWeek != DayOfWeek.SUNDAY
    val hour = local.hour
    val open = isWeekday && hour >= session.openHour && hour < session.closeHour
    return SessionStatus(session, open, local.format(timeFormatter))
}

fun tehranTime(now: Instant): String =
    now.atZone(ZoneId.of("Asia/Tehran")).format(timeFormatter)
