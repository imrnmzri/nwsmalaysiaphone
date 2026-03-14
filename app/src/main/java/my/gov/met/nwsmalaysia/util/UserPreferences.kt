package my.gov.met.nwsmalaysia.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_LOCATION_ID = stringPreferencesKey("selected_location_id")
        val KEY_LOCATION_NAME = stringPreferencesKey("selected_location_name")
        val KEY_LOCATION_STATE = stringPreferencesKey("selected_location_state")
        val KEY_NOTIF_WARNINGS = booleanPreferencesKey("notif_warnings")
    }

    val selectedLocationId: Flow<String?> = context.dataStore.data.map { it[KEY_LOCATION_ID] }
    val selectedLocationName: Flow<String?> = context.dataStore.data.map { it[KEY_LOCATION_NAME] }
    val selectedLocationState: Flow<String?> = context.dataStore.data.map { it[KEY_LOCATION_STATE] }
    val notifWarnings: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIF_WARNINGS] ?: true }

    suspend fun saveLocation(id: String, name: String, state: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOCATION_ID] = id
            prefs[KEY_LOCATION_NAME] = name
            prefs[KEY_LOCATION_STATE] = state
        }
    }

    suspend fun setNotifWarnings(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIF_WARNINGS] = enabled }
    }
}
