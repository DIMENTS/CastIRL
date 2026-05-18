package com.signalscreencaster.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class StreamProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Default",
    val connection: ConnectionConfig = ConnectionConfig(),
    val video: VideoConfig = VideoConfig(),
    val audio: AudioConfig = AudioConfig(),
    val behavior: BehaviorConfig = BehaviorConfig()
)

@Serializable
data class ConnectionConfig(
    val protocol: Protocol = Protocol.RTMP,
    val rtmpUrl: String = "rtmp://",
    val rtmpStreamKey: String = "",
    val rtmpUser: String = "",
    val rtmpPassword: String = "",
    val srtUrl: String = "srt://",
    val srtLatencyUs: Int = 120_000,
    val srtPassphrase: String = "",
    val srtPbkeylen: Int = 128
)

@Serializable
data class VideoConfig(
    val codec: VideoCodecPref = VideoCodecPref.H264,
    val width: Int = 1280,
    val height: Int = 720,
    val fps: Int = 30,
    val bitrateBps: Int = 4_000_000,
    val keyframeIntervalS: Int = 2,
    val hardwareEncoding: Boolean = true
)

@Serializable
data class AudioConfig(
    val source: AudioSourcePref = AudioSourcePref.MICROPHONE,
    val bitrateBps: Int = 128_000,
    val sampleRate: Int = 44100,
    val stereo: Boolean = true,
    val echoCanceler: Boolean = false,
    val noiseSuppressor: Boolean = false
)

@Serializable
data class BehaviorConfig(
    val reconnectOnDisconnect: Boolean = true,
    val reconnectDelayMs: Long = 3_000L,
    val maxReconnectAttempts: Int = 10
)

@Serializable
enum class Protocol { RTMP, SRT }

@Serializable
enum class VideoCodecPref { H264, H265, AV1 }

@Serializable
enum class AudioSourcePref { NONE, MICROPHONE, SYSTEM, BOTH }
