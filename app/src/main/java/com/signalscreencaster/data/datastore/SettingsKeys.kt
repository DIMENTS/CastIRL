package com.signalscreencaster.data.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val ACTIVE_PROFILE_JSON = stringPreferencesKey("active_profile_json")
    val PROFILES_LIST_JSON  = stringPreferencesKey("profiles_list_json")
}
