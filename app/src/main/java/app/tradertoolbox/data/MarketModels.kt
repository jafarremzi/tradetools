package app.tradertoolbox.data

/** وضعیت یک بخش از صفحه «بازار امروز». */
sealed class Section<out T> {
    data object Loading : Section<Nothing>()
    data class Ready<out T>(val value: T) : Section<T>()
    data class Failed(val message: String) : Section<Nothing>()
}

data class FearGreed(val value: Int, val label: String)
data class BtcPrice(val usd: Double?, val eur: Double?)
data class BtcFees(
    val fastest: Double,
    val halfHour: Double,
    val hour: Double,
    val economy: Double,
)
data class ChainTvl(val name: String, val tvl: Double)
data class RateQuote(val code: String, val rate: Double)
data class EcbRates(val date: String, val base: String, val quotes: List<RateQuote>)

data class MarketState(
    val fearGreed: Section<FearGreed> = Section.Loading,
    val btcPrice: Section<BtcPrice> = Section.Loading,
    val btcFees: Section<BtcFees> = Section.Loading,
    val chains: Section<List<ChainTvl>> = Section.Loading,
    val ecb: Section<EcbRates> = Section.Loading,
    val lastUpdatedMillis: Long = 0L,
    val refreshing: Boolean = false,
)
