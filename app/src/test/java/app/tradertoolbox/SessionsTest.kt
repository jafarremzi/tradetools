package app.tradertoolbox

import app.tradertoolbox.logic.marketSessions
import app.tradertoolbox.logic.sessionStatus
import app.tradertoolbox.logic.tehranTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class SessionsTest {

    private fun openMap(instant: String): Map<String, Boolean> {
        val now = Instant.parse(instant)
        return marketSessions.associate { it.name to sessionStatus(it, now).isOpen }
    }

    @Test
    fun wednesday_10utc_onlyLondonOpen() {
        val m = openMap("2026-11-04T10:00:00Z")
        assertEquals(true, m["لندن"])
        assertEquals(false, m["نیویورک"])
        assertEquals(false, m["توکیو"])
        assertEquals(false, m["سیدنی"])
    }

    @Test
    fun wednesday_15utc_londonAndNewYorkOverlap() {
        val m = openMap("2026-11-04T15:00:00Z")
        assertEquals(true, m["لندن"])
        assertEquals(true, m["نیویورک"])
        assertEquals(false, m["توکیو"])
    }

    @Test
    fun wednesday_03utc_asiaOpen() {
        val m = openMap("2026-11-04T03:00:00Z")
        assertEquals(true, m["توکیو"])
        assertEquals(true, m["سیدنی"])
        assertEquals(false, m["لندن"])
    }

    @Test
    fun saturday_allClosed() {
        val m = openMap("2026-11-07T10:00:00Z")
        assertEquals(false, m["لندن"])
        assertEquals(false, m["نیویورک"])
    }

    @Test
    fun tehranIsUtcPlus330() {
        assertEquals("13:30", tehranTime(Instant.parse("2026-11-04T10:00:00Z")))
    }
}
