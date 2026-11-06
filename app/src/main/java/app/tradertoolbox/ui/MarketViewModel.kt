package app.tradertoolbox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tradertoolbox.data.MarketRepository
import app.tradertoolbox.data.MarketState
import app.tradertoolbox.data.Section
import app.tradertoolbox.logic.shouldRefresh
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MarketViewModel : ViewModel() {

    private val repository = MarketRepository()
    private val _state = MutableStateFlow(MarketState())
    val state: StateFlow<MarketState> = _state.asStateFlow()

    /** اگر دریافت شروع شد true برمی‌گرداند. در فاصله‌های کوتاه درخواست جدید نادیده گرفته می‌شود. */
    fun refresh(force: Boolean): Boolean {
        val current = _state.value
        if (current.refreshing) return false
        if (!shouldRefresh(System.currentTimeMillis(), current.lastUpdatedMillis, force)) return false

        _state.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            try {
                coroutineScope {
                    launch {
                        val r = runCatching { repository.fetchFearGreed() }
                        _state.update { it.copy(fearGreed = resolve(it.fearGreed, r)) }
                    }
                    launch {
                        val r = runCatching { repository.fetchBtcPrice() }
                        _state.update { it.copy(btcPrice = resolve(it.btcPrice, r)) }
                    }
                    launch {
                        val r = runCatching { repository.fetchBtcFees() }
                        _state.update { it.copy(btcFees = resolve(it.btcFees, r)) }
                    }
                    launch {
                        val r = runCatching { repository.fetchChains() }
                        _state.update { it.copy(chains = resolve(it.chains, r)) }
                    }
                    launch {
                        val r = runCatching { repository.fetchEcbRates() }
                        _state.update { it.copy(ecb = resolve(it.ecb, r)) }
                    }
                }
            } finally {
                _state.update { s ->
                    val anyReady = s.fearGreed is Section.Ready ||
                        s.btcPrice is Section.Ready ||
                        s.btcFees is Section.Ready ||
                        s.chains is Section.Ready ||
                        s.ecb is Section.Ready
                    s.copy(
                        refreshing = false,
                        lastUpdatedMillis = if (anyReady) System.currentTimeMillis() else 0L,
                    )
                }
            }
        }
        return true
    }

    /** اگر دریافت جدید موفق بود جایگزین می‌شود؛ اگر نه و نسخه قبلی هست، همان می‌ماند. */
    private fun <T> resolve(old: Section<T>, result: Result<T>): Section<T> {
        val value = result.getOrNull()
        return when {
            value != null -> Section.Ready(value)
            old is Section.Ready -> old
            else -> Section.Failed("دریافت نشد. اتصال اینترنت یا دسترسی به این سرویس را بررسی کنید.")
        }
    }
}
