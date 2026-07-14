package com.castIRL.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaCodecInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pedro.common.ConnectChecker
import com.pedro.common.VideoCodec
import com.pedro.library.generic.GenericDisplay
import com.castIRL.data.model.AudioSourcePref
import com.castIRL.data.model.Protocol
import com.castIRL.data.model.StreamProfile
import com.castIRL.data.model.VideoCodecPref
import com.castIRL.streaming.ConnectionState
import com.castIRL.streaming.StreamStats
import com.castIRL.util.SrtUrlBuilder
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

    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "CastIRL:StreamingWakeLock"
        )
        display = GenericDisplay(this, false, this)
        display.setFpsListener { fps ->
            _stats.value = _stats.value.copy(fps = fps.toInt())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            StreamNotificationManager.ACTION_STOP -> {
                stopStream()
                stopSelf()
                return START_NOT_STICKY
            }
            null -> {
                // System restarted the service after kill — re-establish foreground.
                startForegroundCompat()
            }
            // Normal start: startForeground is called from startStreamingSession()
            // after the user has granted MediaProjection consent (API 34 requirement).
        }
        return START_STICKY
    }

    private fun startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                StreamNotificationManager.NOTIFICATION_ID,
                notificationManager.buildNotification(_connectionState.value, _stats.value),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(
                StreamNotificationManager.NOTIFICATION_ID,
                notificationManager.buildNotification(_connectionState.value, _stats.value)
            )
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    // --- Public API ---

    fun getScreenCaptureIntent(): Intent = display.sendIntent()

    /**
     * Called after the user grants MediaProjection consent (RESULT_OK from the capture intent).
     * Calls startForeground with the correct type AFTER consent — required on API 34+.
     */
    fun startStreamingSession(resultCode: Int, data: Intent, profile: StreamProfile) {
        try {
            display.setIntentResult(resultCode, data)
        } catch (e: Exception) {
            // Happens on Android 14+ when the user picks "single app" instead of
            // "entire screen" in the MediaProjection chooser. Only full-screen is supported.
            _connectionState.value = ConnectionState.Error(
                "Select \"Entire screen\" in the screen share dialog — single-app mode is not supported"
            )
            return
        }
        startForegroundCompat()
        startStream(profile)
    }

    fun startStream(profile: StreamProfile) {
        currentProfile = profile
        reconnectAttempts = 0

        val videoConfig = profile.video
        val audioConfig = profile.audio
        val connConfig  = profile.connection

        try {
            display.setVideoCodec(
                when (videoConfig.codec) {
                    VideoCodecPref.H264 -> VideoCodec.H264
                    VideoCodecPref.H265 -> VideoCodec.H265
                    VideoCodecPref.AV1  -> VideoCodec.AV1
                }
            )
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Codec not supported: ${videoConfig.codec.name}")
            return
        }

        val dm = resources.displayMetrics
        val dpi = dm.densityDpi
        val encWidth  = if (videoConfig.useNativeResolution) dm.widthPixels.roundToEven()
                        else videoConfig.width
        val encHeight = if (videoConfig.useNativeResolution) dm.heightPixels.roundToEven()
                        else videoConfig.height

        val videoOk = try {
            display.prepareVideo(
                encWidth,
                encHeight,
                videoConfig.fps,
                videoConfig.bitrateBps,
                0, // rotation
                dpi,
                MediaCodecInfo.CodecProfileLevel.AVCProfileHigh,
                MediaCodecInfo.CodecProfileLevel.AVCLevel31,
                videoConfig.keyframeIntervalS
            )
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Video encoder init failed")
            return
        }

        val audioOk = when (audioConfig.source) {
            AudioSourcePref.NONE -> true
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
                display.prepareAudio(
                    audioConfig.bitrateBps,
                    audioConfig.sampleRate,
                    audioConfig.stereo,
                    audioConfig.echoCanceler,
                    audioConfig.noiseSuppressor
                )
            }
            AudioSourcePref.BOTH -> {
                // Falls back to mic — full mixer is a future feature
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
        if (!wakeLock.isHeld) wakeLock.acquire()
        startStatsPolling()
    }

    fun stopStream() {
        statsJob?.cancel()
        try { display.stopStream() } catch (_: Exception) {}
        if (wakeLock.isHeld) wakeLock.release()
        _connectionState.value = ConnectionState.Idle
        _stats.value = StreamStats()
        notificationManager.update(ConnectionState.Idle, StreamStats())
    }

    fun setVideoBitrateOnFly(bitrateBps: Int) {
        display.setVideoBitrateOnFly(bitrateBps)
    }

    // --- Internal ---

    private fun startStatsPolling() {
        statsJob?.cancel()
        statsJob = lifecycleScope.launch {
            while (isActive) {
                delay(1_000)
                if (_connectionState.value !is ConnectionState.Connected &&
                    _connectionState.value !is ConnectionState.Connecting) break
                val client = display.getStreamClient()
                _stats.value = _stats.value.copy(
                    sentFrames    = client.getSentVideoFrames().toInt(),
                    droppedFrames = client.getDroppedVideoFrames().toInt(),
                    bytesSent     = client.getBytesSend(),
                    durationMs    = System.currentTimeMillis() - streamStartTimeMs
                )
                notificationManager.update(_connectionState.value, _stats.value)
            }
        }
    }

    // --- ConnectChecker (called on background thread — StateFlow.value is thread-safe) ---

    override fun onConnectionStarted(url: String) {
        _connectionState.value = ConnectionState.Connecting
    }

    override fun onConnectionSuccess() {
        reconnectAttempts = 0
        _connectionState.value = ConnectionState.Connected
        notificationManager.update(ConnectionState.Connected, _stats.value)
    }

    override fun onConnectionFailed(reason: String) {
        val profile = currentProfile ?: run {
            _connectionState.value = ConnectionState.Error(reason)
            notificationManager.update(ConnectionState.Error(reason), _stats.value)
            return
        }
        val behavior = profile.behavior
        if (behavior.reconnectOnDisconnect && reconnectAttempts < behavior.maxReconnectAttempts) {
            reconnectAttempts++
            val retried = display.getStreamClient().reTry(behavior.reconnectDelayMs, reason, null)
            if (!retried) {
                _connectionState.value = ConnectionState.Error(reason)
                notificationManager.update(ConnectionState.Error(reason), _stats.value)
            }
        } else {
            statsJob?.cancel()
            _connectionState.value = ConnectionState.Error(reason)
            notificationManager.update(ConnectionState.Error(reason), _stats.value)
        }
    }

    override fun onDisconnect() {
        statsJob?.cancel()
        _connectionState.value = ConnectionState.Disconnected
        notificationManager.update(ConnectionState.Disconnected, _stats.value)
    }

    override fun onAuthError() {
        _connectionState.value = ConnectionState.Error("Authentication failed")
        notificationManager.update(ConnectionState.Error("Authentication failed"), _stats.value)
    }

    override fun onAuthSuccess() {
        _connectionState.value = ConnectionState.Connected
    }

    override fun onNewBitrate(bitrate: Long) {
        _stats.value = _stats.value.copy(bitrateBps = bitrate)
    }

    override fun onDestroy() {
        statsJob?.cancel()
        try { display.stopStream() } catch (_: Exception) {}
        if (::wakeLock.isInitialized && wakeLock.isHeld) wakeLock.release()
        notificationManager.release()
        super.onDestroy()
    }
}

private fun Int.roundToEven() = if (this % 2 == 0) this else this - 1
