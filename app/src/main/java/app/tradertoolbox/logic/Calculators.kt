package app.tradertoolbox.logic

import kotlin.math.abs

sealed class CalcResult<out T> {
    data class Ok<out T>(val value: T) : CalcResult<T>()
    data class Error(val message: String) : CalcResult<Nothing>()
}

enum class Direction { LONG, SHORT }

data class ForexPosition(val riskAmount: Double, val lots: Double)
data class PricePosition(val riskAmount: Double, val units: Double, val positionValue: Double)
data class RiskReward(
    val risk: Double,
    val reward: Double,
    val ratio: Double,
    val breakEvenWinRatePct: Double,
    val isLong: Boolean,
)
data class ProfitLoss(val gross: Double, val fees: Double, val net: Double, val netPct: Double)
data class Liquidation(val price: Double, val distancePct: Double)

object Calculators {

    /** حجم پوزیشن فارکس بر حسب لات: (موجودی × درصد ریسک) ÷ (حد ضرر به پیپ × ارزش هر پیپ برای یک لات). */
    fun forexLots(
        balance: Double,
        riskPct: Double,
        stopLossPips: Double,
        pipValuePerLot: Double,
    ): CalcResult<ForexPosition> {
        if (balance <= 0) return CalcResult.Error("موجودی باید بزرگ‌تر از صفر باشد.")
        if (riskPct <= 0 || riskPct > 100) return CalcResult.Error("درصد ریسک باید بین ۰ و ۱۰۰ باشد.")
        if (stopLossPips <= 0) return CalcResult.Error("حد ضرر (پیپ) باید بزرگ‌تر از صفر باشد.")
        if (pipValuePerLot <= 0) return CalcResult.Error("ارزش هر پیپ باید بزرگ‌تر از صفر باشد.")
        val riskAmount = balance * riskPct / 100.0
        val lots = riskAmount / (stopLossPips * pipValuePerLot)
        return CalcResult.Ok(ForexPosition(riskAmount, lots))
    }

    /** حجم پوزیشن بر اساس قیمت (کریپتو و سهام): مبلغ ریسک ÷ فاصله ورود تا حد ضرر. */
    fun unitsByPrice(
        balance: Double,
        riskPct: Double,
        entry: Double,
        stop: Double,
    ): CalcResult<PricePosition> {
        if (balance <= 0) return CalcResult.Error("موجودی باید بزرگ‌تر از صفر باشد.")
        if (riskPct <= 0 || riskPct > 100) return CalcResult.Error("درصد ریسک باید بین ۰ و ۱۰۰ باشد.")
        if (entry <= 0 || stop <= 0) return CalcResult.Error("قیمت‌ها باید بزرگ‌تر از صفر باشند.")
        val distance = abs(entry - stop)
        if (distance == 0.0) return CalcResult.Error("قیمت ورود و حد ضرر نباید برابر باشند.")
        val riskAmount = balance * riskPct / 100.0
        val units = riskAmount / distance
        return CalcResult.Ok(PricePosition(riskAmount, units, units * entry))
    }

    /** نسبت ریسک به ریوارد. حد ضرر و حد سود باید در دو طرف قیمت ورود باشند. */
    fun riskReward(entry: Double, stop: Double, target: Double): CalcResult<RiskReward> {
        if (entry <= 0 || stop <= 0 || target <= 0) return CalcResult.Error("قیمت‌ها باید بزرگ‌تر از صفر باشند.")
        val isLong = target > entry && entry > stop
        val isShort = target < entry && entry < stop
        if (!isLong && !isShort) {
            return CalcResult.Error("حد ضرر و حد سود باید در دو طرف قیمت ورود باشند.")
        }
        val risk = abs(entry - stop)
        val reward = abs(target - entry)
        val ratio = reward / risk
        val breakEven = 100.0 / (1.0 + ratio)
        return CalcResult.Ok(RiskReward(risk, reward, ratio, breakEven, isLong))
    }

    /** سود و زیان خالص با کارمزد (درصد از ارزش معامله، برای هر سمت ورود و خروج). */
    fun profitLoss(
        direction: Direction,
        entry: Double,
        exit: Double,
        size: Double,
        feePctPerSide: Double,
    ): CalcResult<ProfitLoss> {
        if (entry <= 0 || exit <= 0) return CalcResult.Error("قیمت‌ها باید بزرگ‌تر از صفر باشند.")
        if (size <= 0) return CalcResult.Error("حجم باید بزرگ‌تر از صفر باشد.")
        if (feePctPerSide < 0) return CalcResult.Error("کارمزد نمی‌تواند منفی باشد.")
        val sign = if (direction == Direction.LONG) 1.0 else -1.0
        val gross = (exit - entry) * size * sign
        val fees = (entry * size + exit * size) * feePctPerSide / 100.0
        val net = gross - fees
        val netPct = net / (entry * size) * 100.0
        return CalcResult.Ok(ProfitLoss(gross, fees, net, netPct))
    }

    /**
     * قیمت تقریبی لیکوئید شدن (مارجین ایزوله، قرارداد خطی USDT).
     * فرمول واقعی هر صرافی متفاوت است؛ این فقط یک برآورد است.
     */
    fun liquidationPrice(
        direction: Direction,
        entry: Double,
        leverage: Double,
        maintenanceMarginPct: Double,
    ): CalcResult<Liquidation> {
        if (entry <= 0) return CalcResult.Error("قیمت ورود باید بزرگ‌تر از صفر باشد.")
        if (leverage < 1) return CalcResult.Error("لوریج باید حداقل ۱ باشد.")
        if (maintenanceMarginPct < 0 || maintenanceMarginPct >= 50) {
            return CalcResult.Error("مارجین نگهداری باید بین ۰ و ۵۰ درصد باشد.")
        }
        val mmr = maintenanceMarginPct / 100.0
        val price = if (direction == Direction.LONG) {
            entry * (1.0 - 1.0 / leverage + mmr)
        } else {
            entry * (1.0 + 1.0 / leverage - mmr)
        }
        val distance = abs(price - entry) / entry * 100.0
        return CalcResult.Ok(Liquidation(price, distance))
    }
}
