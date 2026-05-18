package com.signalscreencaster.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.signalscreencaster.data.datastore.SettingsKeys
import com.signalscreencaster.data.model.StreamProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val activeProfile: Flow<StreamProfile> = dataStore.data
        .map { prefs ->
            prefs[SettingsKeys.ACTIVE_PROFILE_JSON]
                ?.let { runCatching { Json.decodeFromString<StreamProfile>(it) }.getOrNull() }
                ?: StreamProfile()
        }

    suspend fun save(profile: StreamProfile) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.ACTIVE_PROFILE_JSON] = Json.encodeToString(profile)
        }
    }
}
