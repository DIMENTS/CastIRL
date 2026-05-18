package com.signalscreencaster.service

import android.content.Intent
import android.media.MediaCodecInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pedro.common.ConnectChecker
import com.pedro.encoder.utils.CodecUtil
import com.pedro.library.generic.GenericDisplay
import com.signalscreencaster.data.model.AudioSourcePref
import com.signalscreencaster.data.model.Protocol
import com.signalscreencaster.data.model.StreamProfile
import com.signalscreencaster.data.model.VideoCodecPref
import com.signalscreencaster.streaming.ConnectionState
import com.signalscreencaster.streaming.StreamStats
import com.signalscreencaster.util.SrtUrlBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StreamingService : LifecycleService(), ConnectChecker {

    @Inject lateinit var notificationManager: StreamNotificationManager

    private lateinit var display: GenericDisplay

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _stats = MutableStateFlow(StreamStats())
    val stats: StateFlow<StreamStats> = _stats.asStateFlow()

    private var streamStartTimeMs = 0L
    private var reconnectAttempts = 0
    private var currentProfile: StreamProfile? = null
    private var statsJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): StreamingService = this@StreamingService
    }

    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        display = GenericDisplay(this, false, this)
        display.setFpsListener { fps ->
            _stats.value = _stats.value.copy(fps = fps.toInt())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == StreamNotificationManager.ACTION_STOP) {
            stopStream()
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(
            StreamNotificationManager.NOTIFICATION_ID,
            notificationManager.buildNotification(ConnectionState.Idle, 0L)
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    // --- Public API ---

    fun getScreenCaptureIntent(): Intent = display.sendIntent()

    fun setIntentResult(resultCode: Int, data: Intent) {
        display.setIntentResult(resultCode, data)
    }

    fun startStream(profile: StreamProfile) {
        currentProfile = profile
        reconnectAttempts = 0

        val videoConfig = profile.video
        val audioConfig = profile.audio
        val connConfig  = profile.connection

        display.setVideoCodec(
            when (videoConfig.codec) {
                VideoCodecPref.H264 -> CodecUtil.VideoCodec.H264
                VideoCodecPref.H265 -> CodecUtil.VideoCodec.H265
                VideoCodecPref.AV1  -> CodecUtil.VideoCodec.AV1
            }
        )

        if (videoConfig.hardwareEncoding) {
            display.forceCodecType(CodecUtil.CodecType.HARDWARE, CodecUtil.CodecType.HARDWARE)
        }

        val dpi = resources.displayMetrics.densityDpi

        val videoOk = display.prepareVideo(
            videoConfig.width,
            videoConfig.height,
            videoConfig.fps,
            videoConfig.bitrateBps,
            0, // rotation
            dpi,
            videoConfig.keyframeIntervalS
        )

        val audioOk = when (audioConfig.source) {
            AudioSourcePref.NONE -> {
                display.getStreamClient().setOnlyVideo(true)
                true
            }
            AudioSourcePref.MICROPHONE -> display.prepareAudio(
                audioConfig.bitrateBps,
                audioConfig.sampleRate,
                audioConfig.stereo,
                audioConfig.echoCanceler,
                audioConfig.noiseSuppressor
            )
            AudioSourcePref.SYSTEM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                display.prepareInternalAudio(
                    audioConfig.bitrateBps,
                    audioConfig.sampleRate,
                    audioConfig.stereo,
                    audioConfig.echoCanceler,
                    audioConfig.noiseSuppressor
                )
            } else {
                // System audio not available on API < 29 — fall back to mic
                display.prepareAudio(
                    audioConfig.bitrateBps,
                    audioConfig.sampleRate,
                    audioConfig.stereo,
                    audioConfig.echoCanceler,
                    audioConfig.noiseSuppressor
                )
            }
            AudioSourcePref.BOTH -> {
                // Custom AudioMixer not yet implemented — falls back to mic
                display.prepareAudio(
                    audioConfig.bitrateBps,
                    audioConfig.sampleRate,
                    audioConfig.stereo,
                    audioConfig.echoCanceler,
                    audioConfig.noiseSuppressor
                )
            }
        }

        if (!videoOk || !audioOk) {
            _connectionState.value = ConnectionState.Error("Encoder preparation failed")
            return
        }

        if (connConfig.protocol == Protocol.RTMP && connConfig.rtmpUser.isNotBlank()) {
            display.getStreamClient().setAuthorization(connConfig.rtmpUser, connConfig.rtmpPassword)
        }

        val url = when (connConfig.protocol) {
            Protocol.RTMP -> "${connConfig.rtmpUrl.trimEnd('/')}/${connConfig.rtmpStreamKey}".trimEnd('/')
            Protocol.SRT  -> SrtUrlBuilder.build(connConfig)
        }

        streamStartTimeMs = System.currentTimeMillis()
        _connectionState.value = ConnectionState.Connecting
        display.startStream(url)
        startStatsPolling()
    }

    fun stopStream() {
        statsJob?.cancel()
        if (display.isStreaming) display.stopStream()
        _connectionState.value = ConnectionState.Idle
        _stats.value = StreamStats()
        notificationManager.update(ConnectionState.Idle, 0L)
    }

    fun setVideoBitrateOnFly(bitrateBps: Int) {
        display.setVideoBitrateOnFly(bitrateBps)
    }

    // --- Internal ---

    private fun startStatsPolling() {
        statsJob?.cancel()
        statsJob = lifecycleScope.launch {
            while (isActive && display.isStreaming) {
                delay(1_000)
                val client = display.getStreamClient()
                _stats.value = _stats.value.copy(
                    sentFrames    = client.getSentVideoFrames(),
                    droppedFrames = client.getDroppedVideoFrames(),
                    bytesSent     = client.getBytesSend(),
                    durationMs    = System.currentTimeMillis() - streamStartTimeMs,
                    hasCongestion = client.hasCongestion()
                )
                notificationManager.update(_connectionState.value, _stats.value.bitrateBps)
            }
        }
    }

    // --- ConnectChecker callbacks (called on background thread — StateFlow.value is thread-safe) ---

    override fun onConnectionStarted(url: String) {
        _connectionState.value = ConnectionState.Connecting
    }

    override fun onConnectionSuccess() {
        reconnectAttempts = 0
        _connectionState.value = ConnectionState.Connected
    }

    override fun onConnectionFailed(reason: String) {
        val profile = currentProfile ?: run {
            _connectionState.value = ConnectionState.Error(reason)
            return
        }
        val behavior = profile.behavior
        if (behavior.reconnectOnDisconnect && reconnectAttempts < behavior.maxReconnectAttempts) {
            reconnectAttempts++
            val retried = display.getStreamClient().reTry(behavior.reconnectDelayMs, reason, null)
            if (!retried) _connectionState.value = ConnectionState.Error(reason)
        } else {
            statsJob?.cancel()
            _connectionState.value = ConnectionState.Error(reason)
        }
    }

    override fun onDisconnect() {
        statsJob?.cancel()
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun onAuthError() {
        _connectionState.value = ConnectionState.Error("Authentication failed")
    }

    override fun onAuthSuccess() {
        _connectionState.value = ConnectionState.Connected
    }

    override fun onNewBitrate(bitrate: Long) {
        _stats.value = _stats.value.copy(bitrateBps = bitrate)
    }

    override fun onDestroy() {
        statsJob?.cancel()
        if (display.isStreaming) display.stopStream()
        super.onDestroy()
    }
}
