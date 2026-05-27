package com.castIRL.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castIRL.data.model.StreamProfile
import com.castIRL.data.repository.SettingsRepository
import com.castIRL.service.StreamingService
import com.castIRL.streaming.ConnectionState
import com.castIRL.streaming.StreamStats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private var service: StreamingService? = null
    private var stateCollectJob: Job? = null
    private var statsCollectJob: Job? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _stats = MutableStateFlow(StreamStats())
    val stats: StateFlow<StreamStats> = _stats.asStateFlow()

    private val _isServiceReady = MutableStateFlow(false)
    val isServiceReady: StateFlow<Boolean> = _isServiceReady.asStateFlow()

    val activeProfile: StateFlow<StreamProfile> = settingsRepo.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamProfile())

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val svc = (binder as StreamingService.LocalBinder).getService()
            service = svc
            _isServiceReady.value = true
            stateCollectJob = viewModelScope.launch { svc.connectionState.collect { _connectionState.value = it } }
            statsCollectJob = viewModelScope.launch { svc.stats.collect { _stats.value = it } }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            stateCollectJob?.cancel()
            statsCollectJob?.cancel()
            service = null
            _isServiceReady.value = false
        }
    }

    fun bindService() {
        val intent = Intent(context, StreamingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        stateCollectJob?.cancel()
        statsCollectJob?.cancel()
        try { context.unbindService(serviceConnection) } catch (_: Exception) {}
        service = null
        _isServiceReady.value = false
    }

    /**
     * Called after RESULT_OK from the MediaProjection consent dialog.
     * Starts the service as foreground (required for process lifetime) and
     * delegates the actual session start — including startForeground() — to the
     * service so it happens after user consent (API 34 requirement).
     */
    fun startStreamingSession(resultCode: Int, data: Intent, profile: StreamProfile) {
        context.startForegroundService(Intent(context, StreamingService::class.java))
        service?.startStreamingSession(resultCode, data, profile)
    }

    fun stopStream() {
        service?.stopStream()
    }

    fun getScreenCaptureIntent(): Intent? = service?.getScreenCaptureIntent()

    fun urlValidationError(profile: StreamProfile): String? {
        val conn = profile.connection
        return when (conn.protocol) {
            com.castIRL.data.model.Protocol.RTMP -> when {
                conn.rtmpUrl.isBlank() || conn.rtmpUrl == "rtmp://" -> "Set RTMP URL in settings"
                conn.rtmpStreamKey.isBlank() -> "Set stream key in settings"
                else -> null
            }
            com.castIRL.data.model.Protocol.SRT -> when {
                conn.srtUrl.isBlank() || conn.srtUrl == "srt://" -> "Set SRT host URL in settings"
                else -> null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}
