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
class ProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val profiles: Flow<List<StreamProfile>> = dataStore.data
        .map { prefs ->
            prefs[SettingsKeys.PROFILES_LIST_JSON]
                ?.let { runCatching { Json.decodeFromString<List<StreamProfile>>(it) }.getOrNull() }
                ?: emptyList()
        }

    suspend fun save(profile: StreamProfile) {
        dataStore.edit { prefs ->
            val current = prefs[SettingsKeys.PROFILES_LIST_JSON]
                ?.let { runCatching { Json.decodeFromString<List<StreamProfile>>(it) }.getOrNull() }
                ?: emptyList()
            val updated = current.filter { it.id != profile.id } + profile
            prefs[SettingsKeys.PROFILES_LIST_JSON] = Json.encodeToString(updated)
        }
    }

    suspend fun delete(profileId: String) {
        dataStore.edit { prefs ->
            val current = prefs[SettingsKeys.PROFILES_LIST_JSON]
                ?.let { runCatching { Json.decodeFromString<List<StreamProfile>>(it) }.getOrNull() }
                ?: emptyList()
            prefs[SettingsKeys.PROFILES_LIST_JSON] = Json.encodeToString(current.filter { it.id != profileId })
        }
    }
}
