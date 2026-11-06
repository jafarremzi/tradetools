package app.tradertoolbox.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.tradertoolbox.data.SettingsStore
import app.tradertoolbox.data.ToolsData
import app.tradertoolbox.data.ToolsRepository
import app.tradertoolbox.logic.MarketFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ToolsRepository(app)
    private val settings = SettingsStore(app)

    val data = MutableStateFlow<ToolsData?>(null)
    val loadError = MutableStateFlow(false)

    val favorites: StateFlow<Set<String>> =
        settings.favorites.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    // مقدار اولیه true است تا قبل از خواندن تنظیمات، پنجره هشدار چشمک نزند.
    val disclaimerAccepted: StateFlow<Boolean> =
        settings.disclaimerAccepted.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    var query by mutableStateOf("")
    var marketFilter by mutableStateOf(MarketFilter.ALL)
    var onlyPersian by mutableStateOf(false)
    var categoryFilter by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
            try {
                data.value = repository.load()
            } catch (e: Exception) {
                loadError.value = true
            }
            if (repository.refreshFromRemote()) {
                try {
                    data.value = repository.load()
                } catch (e: Exception) {
                    // نسخه قبلی باقی می‌ماند
                }
            }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { settings.toggleFavorite(id) }
    }

    fun acceptDisclaimer() {
        viewModelScope.launch { settings.acceptDisclaimer() }
    }
}
