package app.tradertoolbox.logic

import java.time.Instant
import java.time.ZoneId

/** بخشی از محور ۲۴ ساعته (۰ تا ۱) که بازار در آن باز است. */
data class TimelineSegment(val start: Float, val end: Float)

private const val DAY_SECONDS = 86_400
private const val TEHRAN_ZONE = "Asia/Tehran"

/** موقعیت «اکنون» روی محور ۲۴ ساعته به وقت تهران (۰ = نیمه‌شب). */
fun tehranDayFraction(now: Instant): Float {
    val t = now.atZone(ZoneId.of(TEHRAN_ZONE))
    return (t.hour * 3600 + t.minute * 60 + t.second) / DAY_SECONDS.toFloat()
}

/** ساعت کاری سشن را روی محور ۲۴ ساعته به وقت تهران برمی‌گرداند (در صورت عبور از نیمه‌شب، دو بخش). */
fun sessionSegments(session: MarketSession, now: Instant): List<TimelineSegment> {
    val sessionOffset = ZoneId.of(session.zoneId).rules.getOffset(now).totalSeconds
    val tehranOffset = ZoneId.of(TEHRAN_ZONE).rules.getOffset(now).totalSeconds
    val startSec = Math.floorMod(session.openHour * 3600 - sessionOffset + tehranOffset, DAY_SECONDS)
    val endSec = startSec + (session.closeHour - session.openHour) * 3600
    val day = DAY_SECONDS.toFloat()
    return if (endSec <= DAY_SECONDS) {
        listOf(TimelineSegment(startSec / day, endSec / day))
    } else {
        listOf(
            TimelineSegment(startSec / day, 1f),
            TimelineSegment(0f, (endSec - DAY_SECONDS) / day),
        )
    }
}
