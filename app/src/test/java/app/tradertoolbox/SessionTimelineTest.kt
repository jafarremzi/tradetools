package app.tradertoolbox

import app.tradertoolbox.logic.marketSessions
import app.tradertoolbox.logic.sessionSegments
import app.tradertoolbox.logic.tehranDayFraction
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class SessionTimelineTest {

    private val now = Instant.parse("2026-11-04T10:00:00Z") // 13:30 Tehran
    private fun session(name: String) = marketSessions.first { it.name == name }

    @Test
    fun nowFraction_is_1330_in_tehran() {
        assertEquals(0.5625f, tehranDayFraction(now), 1e-4f)
    }

    @Test
    fun london_window_has_single_segment() {
        val s = sessionSegments(session("لندن"), now)
        assertEquals(1, s.size)
        assertEquals(0.4792f, s[0].start, 1e-4f)
        assertEquals(0.8542f, s[0].end, 1e-4f)
    }

    @Test
    fun tokyo_window() {
        val s = sessionSegments(session("توکیو"), now)
        assertEquals(1, s.size)
        assertEquals(0.1458f, s[0].start, 1e-4f)
        assertEquals(0.5208f, s[0].end, 1e-4f)
    }

    @Test
    fun newYork_wraps_past_midnight() {
        val s = sessionSegments(session("نیویورک"), now)
        assertEquals(2, s.size)
        assertEquals(0.6875f, s[0].start, 1e-4f)
        assertEquals(1f, s[0].end, 1e-4f)
        assertEquals(0f, s[1].start, 1e-4f)
        assertEquals(0.0625f, s[1].end, 1e-4f)
    }

    @Test
    fun sydney_wraps_past_midnight() {
        val s = sessionSegments(session("سیدنی"), now)
        assertEquals(2, s.size)
        assertEquals(0.9792f, s[0].start, 1e-4f)
        assertEquals(0f, s[1].start, 1e-4f)
        assertEquals(0.3542f, s[1].end, 1e-4f)
    }
}
