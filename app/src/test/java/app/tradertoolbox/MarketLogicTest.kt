package app.tradertoolbox

import app.tradertoolbox.data.ChainTvl
import app.tradertoolbox.logic.fearGreedLabelFa
import app.tradertoolbox.logic.formatUsdCompact
import app.tradertoolbox.logic.shouldRefresh
import app.tradertoolbox.logic.topChains
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketLogicTest {

    @Test
    fun topChains_sortsDescending_dropsZero_andLimits() {
        val chains = listOf(
            ChainTvl("A", 10.0),
            ChainTvl("B", 0.0),
            ChainTvl("C", 50.0),
            ChainTvl("D", 30.0),
        )
        val top = topChains(chains, 2)
        assertEquals(listOf("C", "D"), top.map { it.name })
    }

    @Test
    fun formatUsdCompact_units() {
        assertEquals("\$1.5B", formatUsdCompact(1.5e9))
        assertEquals("\$2.5T", formatUsdCompact(2.5e12))
        assertEquals("\$3.2M", formatUsdCompact(3_200_000.0))
        assertEquals("\$950,000", formatUsdCompact(950_000.0))
    }

    @Test
    fun fearGreedLabel_translatesKnownAndKeepsUnknown() {
        assertEquals("ترس شدید", fearGreedLabelFa("Extreme Fear"))
        assertEquals("طمع", fearGreedLabelFa("Greed"))
        assertEquals("Something", fearGreedLabelFa("Something"))
    }

    @Test
    fun shouldRefresh_rules() {
        assertTrue(shouldRefresh(1_000_000L, 0L, false))
        assertFalse(shouldRefresh(1_000_000L, 900_000L, false))
        assertTrue(shouldRefresh(1_000_000L, 600_000L, false))
        assertFalse(shouldRefresh(1_000_000L, 980_000L, true))
        assertTrue(shouldRefresh(1_000_000L, 960_000L, true))
    }
}
