package com.castIRL.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.castIRL.data.model.AudioSourcePref
import com.castIRL.data.model.Protocol
import com.castIRL.ui.components.SettingsGroup
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile  by viewModel.profile.collectAsState()
    val conn     = profile.connection
    val video    = profile.video
    val audio    = profile.audio
    val behavior = profile.behavior
    val pid      = profile.id   // used as key so local state resets when a different profile is loaded

    // Local state for text fields — prevents cursor/recomposition issues on every keystroke.
    // Saved to DataStore only on focus loss, not on every character.
    var rtmpUrl        by rememberSaveable(pid) { mutableStateOf(conn.rtmpUrl) }
    var rtmpStreamKey  by rememberSaveable(pid) { mutableStateOf(conn.rtmpStreamKey) }
    var rtmpUser       by rememberSaveable(pid) { mutableStateOf(conn.rtmpUser) }
    var rtmpPassword   by rememberSaveable(pid) { mutableStateOf(conn.rtmpPassword) }
    var srtUrl         by rememberSaveable(pid) { mutableStateOf(conn.srtUrl) }
    var srtStreamId    by rememberSaveable(pid) { mutableStateOf(conn.srtStreamId) }
    var srtLatencyText by rememberSaveable(pid) { mutableStateOf(conn.srtLatencyMs.toString()) }
    var srtPassphrase  by rememberSaveable(pid) { mutableStateOf(conn.srtPassphrase) }
    var reconnectDelay by rememberSaveable(pid) { mutableStateOf(behavior.reconnectDelayMs.toString()) }
    var maxAttempts    by rememberSaveable(pid) { mutableStateOf(behavior.maxReconnectAttempts.toString()) }

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
                    ChipRow("Protocol") {
                        Protocol.entries.forEach { proto ->
                            FilterChip(
                                selected = conn.protocol == proto,
                                onClick  = { viewModel.updateConnection { copy(protocol = proto) } },
                                label    = { Text(proto.name) }
                            )
                        }
                    }

                    if (conn.protocol == Protocol.RTMP) {
                        SettingTextField(
                            label = "RTMP URL",
                            value = rtmpUrl,
                            onValueChange = { rtmpUrl = it },
                            onFocusLost = { viewModel.updateConnection { copy(rtmpUrl = rtmpUrl) } },
                            placeholder = "rtmp://your.server/live"
                        )
                        SettingTextField(
                            label = "Stream Key",
                            value = rtmpStreamKey,
                            onValueChange = { rtmpStreamKey = it },
                            onFocusLost = { viewModel.updateConnection { copy(rtmpStreamKey = rtmpStreamKey) } },
                            placeholder = "your-stream-key"
                        )
                        SettingTextField(
                            label = "Username (optional)",
                            value = rtmpUser,
                            onValueChange = { rtmpUser = it },
                            onFocusLost = { viewModel.updateConnection { copy(rtmpUser = rtmpUser) } }
                        )
                        SettingTextField(
                            label = "Password",
                            value = rtmpPassword,
                            onValueChange = { rtmpPassword = it },
                            onFocusLost = { viewModel.updateConnection { copy(rtmpPassword = rtmpPassword) } },
                            isPassword = true
                        )
                    } else {
                        SettingTextField(
                            label = "SRT Host",
                            value = srtUrl,
                            onValueChange = { srtUrl = it },
                            onFocusLost = { viewModel.updateConnection { copy(srtUrl = srtUrl) } },
                            placeholder = "srt://your.server:9000"
                        )
                        SettingTextField(
                            label = "Stream ID (optional)",
                            value = srtStreamId,
                            onValueChange = { srtStreamId = it },
                            onFocusLost = { viewModel.updateConnection { copy(srtStreamId = srtStreamId) } },
                            placeholder = "e.g. publish/live/mystream"
                        )
                        SettingTextField(
                            label = "Latency (ms)",
                            value = srtLatencyText,
                            onValueChange = { srtLatencyText = it },
                            onFocusLost = {
                                viewModel.updateConnection {
                                    copy(srtLatencyMs = srtLatencyText.toIntOrNull() ?: srtLatencyMs)
                                }
                            },
                            keyboardType = KeyboardType.Number
                        )
                        SettingTextField(
                            label = "Passphrase (optional)",
                            value = srtPassphrase,
                            onValueChange = { srtPassphrase = it },
                            onFocusLost = { viewModel.updateConnection { copy(srtPassphrase = srtPassphrase) } },
                            isPassword = true
                        )
                        if (srtPassphrase.isNotBlank()) {
                            ChipRow("AES Key Length") {
                                listOf(128, 192, 256).forEach { kl ->
                                    FilterChip(
                                        selected = conn.srtPbkeylen == kl,
                                        onClick  = { viewModel.updateConnection { copy(srtPbkeylen = kl) } },
                                        label    = { Text("${kl}-bit") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Video ---
            item {
                SettingsGroup("Video") {
                    ChipRow("Codec") {
                        viewModel.availableCodecs.forEach { codec ->
                            FilterChip(
                                selected = video.codec == codec,
                                onClick  = { viewModel.updateVideo { copy(codec = codec) } },
                                label    = { Text(codec.name) }
                            )
                        }
                    }

                    ChipRow("Resolution") {
                        FilterChip(
                            selected = video.useNativeResolution,
                            onClick  = { viewModel.updateVideo { copy(useNativeResolution = true) } },
                            label    = { Text("Native") }
                        )
                        listOf(
                            Triple(360,  640,  "360p"),
                            Triple(480,  854,  "480p"),
                            Triple(720,  1280, "720p"),
                            Triple(1080, 1920, "1080p")
                        ).forEach { (w, h, label) ->
                            FilterChip(
                                selected = !video.useNativeResolution && video.width == w && video.height == h,
                                onClick  = { viewModel.updateVideo { copy(useNativeResolution = false, width = w, height = h) } },
                                label    = { Text(label) }
                            )
                        }
                    }

                    ChipRow("Frame Rate") {
                        listOf(15, 24, 30, 60).forEach { fps ->
                            FilterChip(
                                selected = video.fps == fps,
                                onClick  = { viewModel.updateVideo { copy(fps = fps) } },
                                label    = { Text("${fps} fps") }
                            )
                        }
                    }

                    ListItem(
                        headlineContent = { Text("Video Bitrate") },
                        supportingContent = {
                            Column {
                                Text(
                                    formatBitrateLabel(video.bitrateBps),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = video.bitrateBps.toFloat(),
                                    onValueChange = { viewModel.updateVideo { copy(bitrateBps = it.roundToInt()) } },
                                    valueRange = 500_000f..20_000_000f
                                )
                            }
                        }
                    )

                    ChipRow("Keyframe Interval") {
                        listOf(1, 2, 5, 10).forEach { s ->
                            FilterChip(
                                selected = video.keyframeIntervalS == s,
                                onClick  = { viewModel.updateVideo { copy(keyframeIntervalS = s) } },
                                label    = { Text("${s}s") }
                            )
                        }
                    }

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
                    ChipRow("Source") {
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

                    if (audio.source != AudioSourcePref.NONE) {
                        ChipRow("Bitrate") {
                            listOf(64_000, 128_000, 192_000, 256_000).forEach { br ->
                                FilterChip(
                                    selected = audio.bitrateBps == br,
                                    onClick  = { viewModel.updateAudio { copy(bitrateBps = br) } },
                                    label    = { Text("${br / 1_000}k") }
                                )
                            }
                        }

                        ChipRow("Sample Rate") {
                            listOf(44100, 48000).forEach { sr ->
                                FilterChip(
                                    selected = audio.sampleRate == sr,
                                    onClick  = { viewModel.updateAudio { copy(sampleRate = sr) } },
                                    label    = { Text("${sr / 1_000} kHz") }
                                )
                            }
                        }

                        ChipRow("Channels") {
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
                            value = reconnectDelay,
                            onValueChange = { reconnectDelay = it },
                            onFocusLost = {
                                viewModel.updateBehavior {
                                    copy(reconnectDelayMs = reconnectDelay.toLongOrNull() ?: reconnectDelayMs)
                                }
                            },
                            keyboardType = KeyboardType.Number
                        )
                        SettingTextField(
                            label = "Max Attempts",
                            value = maxAttempts,
                            onValueChange = { maxAttempts = it },
                            onFocusLost = {
                                viewModel.updateBehavior {
                                    copy(maxReconnectAttempts = maxAttempts.toIntOrNull() ?: maxReconnectAttempts)
                                }
                            },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "v${com.castIRL.BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "By DIMENTS",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit = {},
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    // Guard: only save on focus loss after the field has been interacted with.
    // Without this, onFocusChanged fires with isFocused=false on first composition
    // for every field simultaneously, causing a rapid-save race condition.
    var wasFocused by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    wasFocused = true
                } else if (wasFocused) {
                    onFocusLost()
                }
            }
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
