package com.castIRL.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castIRL.data.model.AudioConfig
import com.castIRL.data.model.BehaviorConfig
import com.castIRL.data.model.ConnectionConfig
import com.castIRL.data.model.StreamProfile
import com.castIRL.data.model.VideoConfig
import com.castIRL.data.repository.SettingsRepository
import com.castIRL.util.CodecChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val profile: StateFlow<StreamProfile> = settingsRepo.activeProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, StreamProfile())

    val availableCodecs = CodecChecker.availableVideoCodecs()

    // Each update reads + writes atomically inside DataStore.edit so concurrent
    // saves (e.g. multiple onFocusLost callbacks firing at once) never overwrite
    // each other's changes.

    fun updateConnection(block: ConnectionConfig.() -> ConnectionConfig) {
        viewModelScope.launch {
            settingsRepo.updateProfile { copy(connection = block(connection)) }
        }
    }

    fun updateVideo(block: VideoConfig.() -> VideoConfig) {
        viewModelScope.launch {
            settingsRepo.updateProfile { copy(video = block(video)) }
        }
    }

    fun updateAudio(block: AudioConfig.() -> AudioConfig) {
        viewModelScope.launch {
            settingsRepo.updateProfile { copy(audio = block(audio)) }
        }
    }

    fun updateBehavior(block: BehaviorConfig.() -> BehaviorConfig) {
        viewModelScope.launch {
            settingsRepo.updateProfile { copy(behavior = block(behavior)) }
        }
    }
}
