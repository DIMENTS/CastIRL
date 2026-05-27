package com.castIRL.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castIRL.data.model.StreamProfile
import com.castIRL.data.repository.ProfileRepository
import com.castIRL.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val profiles: StateFlow<List<StreamProfile>> = profileRepo.profiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeProfile: StateFlow<StreamProfile> = settingsRepo.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamProfile())

    fun saveCurrentAsProfile(name: String) {
        viewModelScope.launch {
            val current = activeProfile.value.copy(name = name)
            profileRepo.save(current)
        }
    }

    fun loadProfile(profile: StreamProfile) {
        viewModelScope.launch {
            settingsRepo.save(profile)
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            profileRepo.delete(profileId)
        }
    }
}
