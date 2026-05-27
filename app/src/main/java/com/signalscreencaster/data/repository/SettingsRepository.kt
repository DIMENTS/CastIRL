package com.castIRL.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.castIRL.data.datastore.SettingsKeys
import com.castIRL.data.model.StreamProfile
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

    // Reads the current stored profile and applies block atomically within a single
    // DataStore transaction. This prevents concurrent updates from overwriting each
    // other when several settings fields save in rapid succession.
    suspend fun updateProfile(block: StreamProfile.() -> StreamProfile) {
        dataStore.edit { prefs ->
            val current = prefs[SettingsKeys.ACTIVE_PROFILE_JSON]
                ?.let { runCatching { Json.decodeFromString<StreamProfile>(it) }.getOrNull() }
                ?: StreamProfile()
            prefs[SettingsKeys.ACTIVE_PROFILE_JSON] = Json.encodeToString(block(current))
        }
    }

    suspend fun save(profile: StreamProfile) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.ACTIVE_PROFILE_JSON] = Json.encodeToString(profile)
        }
    }
}
