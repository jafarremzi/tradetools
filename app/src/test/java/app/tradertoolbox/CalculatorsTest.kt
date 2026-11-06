package app.tradertoolbox

import app.tradertoolbox.logic.CalcResult
import app.tradertoolbox.logic.Calculators
import app.tradertoolbox.logic.Direction
import app.tradertoolbox.logic.normalizeDigits
import app.tradertoolbox.logic.parseNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorsTest {

    @Suppress("UNCHECKED_CAST")
    private fun <T> CalcResult<T>.value(): T {
        assertTrue("expected Ok but was $this", this is CalcResult.Ok<*>)
        return (this as CalcResult.Ok<T>).value
    }

    @Test
    fun forexLots_basic() {
        val r = Calculators.forexLots(10000.0, 1.0, 50.0, 10.0).value()
        assertEquals(100.0, r.riskAmount, 1e-9)
        assertEquals(0.2, r.lots, 1e-9)
    }

    @Test
    fun forexLots_rejectsBadInput() {
        assertTrue(Calculators.forexLots(0.0, 1.0, 50.0, 10.0) is CalcResult.Error)
        assertTrue(Calculators.forexLots(1000.0, 0.0, 50.0, 10.0) is CalcResult.Error)
        assertTrue(Calculators.forexLots(1000.0, 1.0, 0.0, 10.0) is CalcResult.Error)
    }

    @Test
    fun unitsByPrice_basic() {
        val r = Calculators.unitsByPrice(10000.0, 2.0, 100.0, 95.0).value()
        assertEquals(200.0, r.riskAmount, 1e-9)
        assertEquals(40.0, r.units, 1e-9)
        assertEquals(4000.0, r.positionValue, 1e-9)
    }

    @Test
    fun unitsByPrice_equalPricesIsError() {
        assertTrue(Calculators.unitsByPrice(1000.0, 1.0, 100.0, 100.0) is CalcResult.Error)
    }

    @Test
    fun riskReward_long() {
        val r = Calculators.riskReward(100.0, 95.0, 115.0).value()
        assertEquals(5.0, r.risk, 1e-9)
        assertEquals(15.0, r.reward, 1e-9)
        assertEquals(3.0, r.ratio, 1e-9)
        assertEquals(25.0, r.breakEvenWinRatePct, 1e-9)
        assertTrue(r.isLong)
    }

    @Test
    fun riskReward_short() {
        val r = Calculators.riskReward(100.0, 105.0, 90.0).value()
        assertEquals(2.0, r.ratio, 1e-9)
        assertTrue(!r.isLong)
    }

    @Test
    fun riskReward_wrongSidesIsError() {
        assertTrue(Calculators.riskReward(100.0, 105.0, 110.0) is CalcResult.Error)
    }

    @Test
    fun profitLoss_longWithFees() {
        val r = Calculators.profitLoss(Direction.LONG, 100.0, 110.0, 2.0, 0.1).value()
        assertEquals(20.0, r.gross, 1e-9)
        assertEquals(0.42, r.fees, 1e-9)
        assertEquals(19.58, r.net, 1e-9)
    }

    @Test
    fun profitLoss_shortLoses_whenPriceRises() {
        val r = Calculators.profitLoss(Direction.SHORT, 100.0, 110.0, 1.0, 0.0).value()
        assertEquals(-10.0, r.gross, 1e-9)
        assertEquals(-10.0, r.net, 1e-9)
    }

    @Test
    fun liquidation_longAndShort() {
        val l = Calculators.liquidationPrice(Direction.LONG, 100.0, 10.0, 0.5).value()
        assertEquals(90.5, l.price, 1e-9)
        val s = Calculators.liquidationPrice(Direction.SHORT, 100.0, 10.0, 0.5).value()
        assertEquals(109.5, s.price, 1e-9)
    }

    @Test
    fun normalizeDigits_persianAndArabic() {
        assertEquals("123.5", normalizeDigits("۱۲۳٫۵"))
        assertEquals("456", normalizeDigits("٤٥٦"))
    }

    @Test
    fun parseNumber_handlesSeparatorsAndGarbage() {
        assertEquals(1000.0, parseNumber("۱٬۰۰۰")!!, 1e-9)
        assertEquals(2.5, parseNumber("2/5")!!, 1e-9)
        assertNull(parseNumber(""))
        assertNull(parseNumber("abc"))
    }
}
