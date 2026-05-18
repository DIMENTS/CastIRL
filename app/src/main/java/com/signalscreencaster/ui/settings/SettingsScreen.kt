package com.signalscreencaster.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signalscreencaster.data.model.AudioSourcePref
import com.signalscreencaster.data.model.Protocol
import com.signalscreencaster.data.model.VideoCodecPref
import com.signalscreencaster.ui.components.SettingsGroup
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val conn    = profile.connection
    val video   = profile.video
    val audio   = profile.audio
    val behavior = profile.behavior

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 8.dp)
        ) {
            // --- Connection ---
            item {
                SettingsGroup("Connection") {
                    // Protocol toggle
                    ListItem(
                        headlineContent = { Text("Protocol") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Protocol.entries.forEach { proto ->
                                    FilterChip(
                                        selected = conn.protocol == proto,
                                        onClick  = { viewModel.updateConnection { copy(protocol = proto) } },
                                        label    = { Text(proto.name) }
                                    )
                                }
                            }
                        }
                    )

                    if (conn.protocol == Protocol.RTMP) {
                        SettingTextField(
                            label = "RTMP URL",
                            value = conn.rtmpUrl,
                            onValueChange = { viewModel.updateConnection { copy(rtmpUrl = it) } },
                            placeholder = "rtmp://your.server/live"
                        )
                        SettingTextField(
                            label = "Stream Key",
                            value = conn.rtmpStreamKey,
                            onValueChange = { viewModel.updateConnection { copy(rtmpStreamKey = it) } },
                            placeholder = "your-stream-key"
                        )
                        SettingTextField(
                            label = "Username (optional)",
                            value = conn.rtmpUser,
                            onValueChange = { viewModel.updateConnection { copy(rtmpUser = it) } }
                        )
                        SettingTextField(
                            label = "Password",
                            value = conn.rtmpPassword,
                            onValueChange = { viewModel.updateConnection { copy(rtmpPassword = it) } },
                            isPassword = true
                        )
                    } else {
                        SettingTextField(
                            label = "SRT URL",
                            value = conn.srtUrl,
                            onValueChange = { viewModel.updateConnection { copy(srtUrl = it) } },
                            placeholder = "srt://your.server:9000"
                        )
                        SettingTextField(
                            label = "Latency (µs)",
                            value = conn.srtLatencyUs.toString(),
                            onValueChange = {
                                viewModel.updateConnection { copy(srtLatencyUs = it.toIntOrNull() ?: srtLatencyUs) }
                            },
                            keyboardType = KeyboardType.Number
                        )
                        SettingTextField(
                            label = "Passphrase (optional)",
                            value = conn.srtPassphrase,
                            onValueChange = { viewModel.updateConnection { copy(srtPassphrase = it) } },
                            isPassword = true
                        )
                        if (conn.srtPassphrase.isNotBlank()) {
                            ListItem(
                                headlineContent = { Text("Key Length") },
                                trailingContent = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(128, 192, 256).forEach { kl ->
                                            FilterChip(
                                                selected = conn.srtPbkeylen == kl,
                                                onClick  = { viewModel.updateConnection { copy(srtPbkeylen = kl) } },
                                                label    = { Text("$kl") }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // --- Video ---
            item {
                SettingsGroup("Video") {
                    // Codec
                    ListItem(
                        headlineContent = { Text("Codec") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                viewModel.availableCodecs.forEach { codec ->
                                    FilterChip(
                                        selected = video.codec == codec,
                                        onClick  = { viewModel.updateVideo { copy(codec = codec) } },
                                        label    = { Text(codec.name) }
                                    )
                                }
                            }
                        }
                    )

                    // Resolution
                    ListItem(
                        headlineContent = { Text("Resolution") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    Triple(640,  360,  "360p"),
                                    Triple(854,  480,  "480p"),
                                    Triple(1280, 720,  "720p"),
                                    Triple(1920, 1080, "1080p")
                                ).forEach { (w, h, label) ->
                                    FilterChip(
                                        selected = video.width == w && video.height == h,
                                        onClick  = { viewModel.updateVideo { copy(width = w, height = h) } },
                                        label    = { Text(label) }
                                    )
                                }
                            }
                        }
                    )

                    // FPS
                    ListItem(
                        headlineContent = { Text("Frame Rate") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(15, 24, 30, 60).forEach { fps ->
                                    FilterChip(
                                        selected = video.fps == fps,
                                        onClick  = { viewModel.updateVideo { copy(fps = fps) } },
                                        label    = { Text("${fps}fps") }
                                    )
                                }
                            }
                        }
                    )

                    // Bitrate slider (500 kbps – 20 Mbps)
                    ListItem(
                        headlineContent = { Text("Video Bitrate") },
                        supportingContent = {
                            Column {
                                Text(formatBitrateLabel(video.bitrateBps))
                                Slider(
                                    value = video.bitrateBps.toFloat(),
                                    onValueChange = { viewModel.updateVideo { copy(bitrateBps = it.roundToInt()) } },
                                    valueRange = 500_000f..20_000_000f,
                                    steps = 0
                                )
                            }
                        }
                    )

                    // Keyframe interval
                    ListItem(
                        headlineContent = { Text("Keyframe Interval") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 5, 10).forEach { s ->
                                    FilterChip(
                                        selected = video.keyframeIntervalS == s,
                                        onClick  = { viewModel.updateVideo { copy(keyframeIntervalS = s) } },
                                        label    = { Text("${s}s") }
                                    )
                                }
                            }
                        }
                    )

                    // Hardware encoding
                    ListItem(
                        headlineContent = { Text("Hardware Encoding") },
                        trailingContent = {
                            Switch(
                                checked = video.hardwareEncoding,
                                onCheckedChange = { viewModel.updateVideo { copy(hardwareEncoding = it) } }
                            )
                        }
                    )
                }
            }

            // --- Audio ---
            item {
                SettingsGroup("Audio") {
                    // Source
                    ListItem(
                        headlineContent = { Text("Audio Source") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AudioSourcePref.entries
                                    .filter { it != AudioSourcePref.SYSTEM || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q }
                                    .forEach { src ->
                                        FilterChip(
                                            selected = audio.source == src,
                                            onClick  = { viewModel.updateAudio { copy(source = src) } },
                                            label    = { Text(src.label) }
                                        )
                                    }
                            }
                        }
                    )

                    if (audio.source != AudioSourcePref.NONE) {
                        // Audio bitrate
                        ListItem(
                            headlineContent = { Text("Audio Bitrate") },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(64_000, 128_000, 192_000, 256_000).forEach { br ->
                                        FilterChip(
                                            selected = audio.bitrateBps == br,
                                            onClick  = { viewModel.updateAudio { copy(bitrateBps = br) } },
                                            label    = { Text("${br / 1_000}k") }
                                        )
                                    }
                                }
                            }
                        )

                        // Sample rate
                        ListItem(
                            headlineContent = { Text("Sample Rate") },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(44100, 48000).forEach { sr ->
                                        FilterChip(
                                            selected = audio.sampleRate == sr,
                                            onClick  = { viewModel.updateAudio { copy(sampleRate = sr) } },
                                            label    = { Text("${sr / 1_000}kHz") }
                                        )
                                    }
                                }
                            }
                        )

                        // Stereo
                        ListItem(
                            headlineContent = { Text("Channels") },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = audio.stereo,
                                        onClick  = { viewModel.updateAudio { copy(stereo = true) } },
                                        label    = { Text("Stereo") }
                                    )
                                    FilterChip(
                                        selected = !audio.stereo,
                                        onClick  = { viewModel.updateAudio { copy(stereo = false) } },
                                        label    = { Text("Mono") }
                                    )
                                }
                            }
                        )

                        // Echo / noise (only for mic sources)
                        if (audio.source == AudioSourcePref.MICROPHONE || audio.source == AudioSourcePref.BOTH) {
                            ListItem(
                                headlineContent = { Text("Echo Cancellation") },
                                trailingContent = {
                                    Switch(
                                        checked = audio.echoCanceler,
                                        onCheckedChange = { viewModel.updateAudio { copy(echoCanceler = it) } }
                                    )
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Noise Suppression") },
                                trailingContent = {
                                    Switch(
                                        checked = audio.noiseSuppressor,
                                        onCheckedChange = { viewModel.updateAudio { copy(noiseSuppressor = it) } }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // --- Behavior ---
            item {
                SettingsGroup("Stream Behavior") {
                    ListItem(
                        headlineContent = { Text("Auto-Reconnect") },
                        trailingContent = {
                            Switch(
                                checked = behavior.reconnectOnDisconnect,
                                onCheckedChange = { viewModel.updateBehavior { copy(reconnectOnDisconnect = it) } }
                            )
                        }
                    )

                    if (behavior.reconnectOnDisconnect) {
                        SettingTextField(
                            label = "Reconnect Delay (ms)",
                            value = behavior.reconnectDelayMs.toString(),
                            onValueChange = {
                                viewModel.updateBehavior { copy(reconnectDelayMs = it.toLongOrNull() ?: reconnectDelayMs) }
                            },
                            keyboardType = KeyboardType.Number
                        )
                        SettingTextField(
                            label = "Max Attempts",
                            value = behavior.maxReconnectAttempts.toString(),
                            onValueChange = {
                                viewModel.updateBehavior { copy(maxReconnectAttempts = it.toIntOrNull() ?: maxReconnectAttempts) }
                            },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SettingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

private val AudioSourcePref.label get() = when (this) {
    AudioSourcePref.NONE       -> "None"
    AudioSourcePref.MICROPHONE -> "Mic"
    AudioSourcePref.SYSTEM     -> "System"
    AudioSourcePref.BOTH       -> "Both"
}

private fun formatBitrateLabel(bps: Int): String = when {
    bps >= 1_000_000 -> "%.1f Mbps".format(bps / 1_000_000.0)
    else             -> "${bps / 1_000} kbps"
}
