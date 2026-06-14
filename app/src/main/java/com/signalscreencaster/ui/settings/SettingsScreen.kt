package com.castIRL.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.castIRL.data.model.AudioSourcePref
import com.castIRL.data.model.Protocol
import com.castIRL.ui.components.SegmentedOptionGroup
import com.castIRL.ui.components.SettingsGroup
import com.castIRL.ui.components.WavySlider
import com.castIRL.ui.icons.PhosphorIcons
import kotlin.math.roundToInt

private data class ResOption(val label: String, val native: Boolean, val w: Int, val h: Int)

private val RES_OPTIONS = listOf(
    ResOption("Native", true, 0, 0),
    ResOption("360p", false, 360, 640),
    ResOption("480p", false, 480, 854),
    ResOption("720p", false, 720, 1280),
    ResOption("1080p", false, 1080, 1920),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val profile  by viewModel.profile.collectAsState()
    val conn     = profile.connection
    val video    = profile.video
    val audio    = profile.audio
    val behavior = profile.behavior
    val pid      = profile.id

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
            TopAppBar(title = { Text("Settings", style = MaterialTheme.typography.titleLarge) })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        ) {
            // --- Connection ---
            item {
                SettingsGroup("Connection", icon = PhosphorIcons.WifiHigh) {
                    SegmentedOptionGroup(
                        label       = "Protocol",
                        options     = Protocol.entries.toList(),
                        selected    = conn.protocol,
                        onSelect    = { proto -> viewModel.updateConnection { copy(protocol = proto) } },
                        optionLabel = { it.name },
                    )

                    if (conn.protocol == Protocol.RTMP) {
                        SettingTextField("RTMP URL", rtmpUrl, { rtmpUrl = it },
                            { viewModel.updateConnection { copy(rtmpUrl = rtmpUrl) } }, "rtmp://your.server/live")
                        SettingTextField("Stream Key", rtmpStreamKey, { rtmpStreamKey = it },
                            { viewModel.updateConnection { copy(rtmpStreamKey = rtmpStreamKey) } }, "your-stream-key")
                        SettingTextField("Username (optional)", rtmpUser, { rtmpUser = it },
                            { viewModel.updateConnection { copy(rtmpUser = rtmpUser) } })
                        SettingTextField("Password", rtmpPassword, { rtmpPassword = it },
                            { viewModel.updateConnection { copy(rtmpPassword = rtmpPassword) } }, isPassword = true)
                    } else {
                        SettingTextField("SRT Host", srtUrl, { srtUrl = it },
                            { viewModel.updateConnection { copy(srtUrl = srtUrl) } }, "srt://your.server:9000")
                        SettingTextField("Stream ID (optional)", srtStreamId, { srtStreamId = it },
                            { viewModel.updateConnection { copy(srtStreamId = srtStreamId) } }, "e.g. publish/live/mystream")
                        SettingTextField("Latency (ms)", srtLatencyText, { srtLatencyText = it },
                            { viewModel.updateConnection { copy(srtLatencyMs = srtLatencyText.toIntOrNull() ?: srtLatencyMs) } },
                            keyboardType = KeyboardType.Number)
                        SettingTextField("Passphrase (optional)", srtPassphrase, { srtPassphrase = it },
                            { viewModel.updateConnection { copy(srtPassphrase = srtPassphrase) } }, isPassword = true)
                        if (srtPassphrase.isNotBlank()) {
                            SegmentedOptionGroup(
                                label       = "AES Key Length",
                                options     = listOf(128, 192, 256),
                                selected    = conn.srtPbkeylen,
                                onSelect    = { kl -> viewModel.updateConnection { copy(srtPbkeylen = kl) } },
                                optionLabel = { "${it}-bit" },
                            )
                        }
                    }
                }
            }

            // --- Video ---
            item {
                SettingsGroup("Video", icon = PhosphorIcons.VideoCamera) {
                    SegmentedOptionGroup(
                        label       = "Codec",
                        options     = viewModel.availableCodecs,
                        selected    = video.codec,
                        onSelect    = { codec -> viewModel.updateVideo { copy(codec = codec) } },
                        optionLabel = { it.name },
                    )

                    val selectedRes = RES_OPTIONS.firstOrNull {
                        if (it.native) video.useNativeResolution
                        else !video.useNativeResolution && video.width == it.w && video.height == it.h
                    } ?: RES_OPTIONS.first()
                    SegmentedOptionGroup(
                        label       = "Resolution",
                        options     = RES_OPTIONS,
                        selected    = selectedRes,
                        onSelect    = { r ->
                            viewModel.updateVideo {
                                if (r.native) copy(useNativeResolution = true)
                                else copy(useNativeResolution = false, width = r.w, height = r.h)
                            }
                        },
                        optionLabel = { it.label },
                    )

                    SegmentedOptionGroup(
                        label       = "Frame Rate",
                        options     = listOf(15, 24, 30, 60),
                        selected    = video.fps,
                        onSelect    = { fps -> viewModel.updateVideo { copy(fps = fps) } },
                        optionLabel = { "$it fps" },
                    )

                    SliderSettingRow(
                        label = "Video Bitrate",
                        value = formatBitrateLabel(video.bitrateBps),
                    ) {
                        WavySlider(
                            value         = video.bitrateBps.toFloat(),
                            onValueChange = { viewModel.updateVideo { copy(bitrateBps = it.roundToInt()) } },
                            valueRange    = 500_000f..20_000_000f,
                        )
                    }

                    SegmentedOptionGroup(
                        label       = "Keyframe Interval",
                        options     = listOf(1, 2, 5, 10),
                        selected    = video.keyframeIntervalS,
                        onSelect    = { s -> viewModel.updateVideo { copy(keyframeIntervalS = s) } },
                        optionLabel = { "${it}s" },
                    )

                    SwitchRow("Hardware Encoding", video.hardwareEncoding) {
                        viewModel.updateVideo { copy(hardwareEncoding = it) }
                    }
                }
            }

            // --- Audio ---
            item {
                SettingsGroup("Audio", icon = PhosphorIcons.Microphone) {
                    SegmentedOptionGroup(
                        label    = "Source",
                        options  = AudioSourcePref.entries
                            .filter { it != AudioSourcePref.SYSTEM || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q },
                        selected = audio.source,
                        onSelect = { src -> viewModel.updateAudio { copy(source = src) } },
                        optionLabel = { it.label },
                    )

                    if (audio.source == AudioSourcePref.BOTH) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = {
                                Text(
                                    "\"Both\" currently falls back to microphone only. Full mic + system audio mixing is planned for a future release.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            leadingContent = {
                                Icon(
                                    PhosphorIcons.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                    }

                    if (audio.source != AudioSourcePref.NONE) {
                        SegmentedOptionGroup(
                            label       = "Bitrate",
                            options     = listOf(64_000, 128_000, 192_000, 256_000),
                            selected    = audio.bitrateBps,
                            onSelect    = { br -> viewModel.updateAudio { copy(bitrateBps = br) } },
                            optionLabel = { "${it / 1_000}k" },
                        )
                        SegmentedOptionGroup(
                            label       = "Sample Rate",
                            options     = listOf(44100, 48000),
                            selected    = audio.sampleRate,
                            onSelect    = { sr -> viewModel.updateAudio { copy(sampleRate = sr) } },
                            optionLabel = { "${it / 1_000} kHz" },
                        )
                        SegmentedOptionGroup(
                            label       = "Channels",
                            options     = listOf(true, false),
                            selected    = audio.stereo,
                            onSelect    = { stereo -> viewModel.updateAudio { copy(stereo = stereo) } },
                            optionLabel = { if (it) "Stereo" else "Mono" },
                        )

                        if (audio.source == AudioSourcePref.MICROPHONE || audio.source == AudioSourcePref.BOTH) {
                            SwitchRow("Echo Cancellation", audio.echoCanceler) {
                                viewModel.updateAudio { copy(echoCanceler = it) }
                            }
                            SwitchRow("Noise Suppression", audio.noiseSuppressor) {
                                viewModel.updateAudio { copy(noiseSuppressor = it) }
                            }
                        }
                    }
                }
            }

            // --- Behavior ---
            item {
                SettingsGroup("Stream Behavior", icon = PhosphorIcons.SlidersHorizontal) {
                    SwitchRow("Auto-Reconnect", behavior.reconnectOnDisconnect) {
                        viewModel.updateBehavior { copy(reconnectOnDisconnect = it) }
                    }
                    if (behavior.reconnectOnDisconnect) {
                        SettingTextField("Reconnect Delay (ms)", reconnectDelay, { reconnectDelay = it },
                            { viewModel.updateBehavior { copy(reconnectDelayMs = reconnectDelay.toLongOrNull() ?: reconnectDelayMs) } },
                            keyboardType = KeyboardType.Number)
                        SettingTextField("Max Attempts", maxAttempts, { maxAttempts = it },
                            { viewModel.updateBehavior { copy(maxReconnectAttempts = maxAttempts.toIntOrNull() ?: maxReconnectAttempts) } },
                            keyboardType = KeyboardType.Number)
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text  = "v${com.castIRL.BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text  = "By DIMENTS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderSettingRow(
    label: String,
    value: String,
    slider: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        slider()
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(label) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

@Composable
private fun SettingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit = {},
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
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
                if (focusState.isFocused) wasFocused = true
                else if (wasFocused) onFocusLost()
            },
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
