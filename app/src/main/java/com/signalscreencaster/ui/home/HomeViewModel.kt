package com.signalscreencaster.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalscreencaster.data.model.StreamProfile
import com.signalscreencaster.data.repository.SettingsRepository
import com.signalscreencaster.service.StreamingService
import com.signalscreencaster.streaming.ConnectionState
import com.signalscreencaster.streaming.StreamStats
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

    fun startForegroundService() {
        context.startForegroundService(Intent(context, StreamingService::class.java))
    }

    fun startStream(profile: StreamProfile) {
        service?.startStream(profile)
    }

    fun stopStream() {
        service?.stopStream()
    }

    fun setIntentResult(resultCode: Int, data: Intent) {
        service?.setIntentResult(resultCode, data)
    }

    fun getScreenCaptureIntent(): Intent? = service?.getScreenCaptureIntent()

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}
