package com.signalscreencaster.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalscreencaster.data.model.AudioConfig
import com.signalscreencaster.data.model.AudioSourcePref
import com.signalscreencaster.data.model.BehaviorConfig
import com.signalscreencaster.data.model.ConnectionConfig
import com.signalscreencaster.data.model.Protocol
import com.signalscreencaster.data.model.StreamProfile
import com.signalscreencaster.data.model.VideoCodecPref
import com.signalscreencaster.data.model.VideoConfig
import com.signalscreencaster.data.repository.SettingsRepository
import com.signalscreencaster.util.CodecChecker
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamProfile())

    val availableCodecs = CodecChecker.availableVideoCodecs()

    fun updateConnection(block: ConnectionConfig.() -> ConnectionConfig) {
        viewModelScope.launch {
            val current = profile.value
            settingsRepo.save(current.copy(connection = block(current.connection)))
        }
    }

    fun updateVideo(block: VideoConfig.() -> VideoConfig) {
        viewModelScope.launch {
            val current = profile.value
            settingsRepo.save(current.copy(video = block(current.video)))
        }
    }

    fun updateAudio(block: AudioConfig.() -> AudioConfig) {
        viewModelScope.launch {
            val current = profile.value
            settingsRepo.save(current.copy(audio = block(current.audio)))
        }
    }

    fun updateBehavior(block: BehaviorConfig.() -> BehaviorConfig) {
        viewModelScope.launch {
            val current = profile.value
            settingsRepo.save(current.copy(behavior = block(current.behavior)))
        }
    }
}
