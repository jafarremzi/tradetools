package app.tradertoolbox.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsStore(context: Context) {
    private val store = context.applicationContext.settingsDataStore
    private val favoritesKey = stringSetPreferencesKey("favorites")
    private val disclaimerKey = booleanPreferencesKey("disclaimer_accepted")

    val favorites: Flow<Set<String>> = store.data.map { prefs -> prefs[favoritesKey] ?: emptySet() }
    val disclaimerAccepted: Flow<Boolean> = store.data.map { prefs -> prefs[disclaimerKey] ?: false }

    suspend fun toggleFavorite(id: String) {
        store.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = if (id in current) current - id else current + id
        }
    }

    suspend fun acceptDisclaimer() {
        store.edit { prefs -> prefs[disclaimerKey] = true }
    }
}
