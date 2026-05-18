package com.signalscreencaster.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signalscreencaster.data.model.Protocol
import com.signalscreencaster.streaming.ConnectionState
import com.signalscreencaster.ui.components.StatsBadge
import com.signalscreencaster.ui.components.StreamButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateSettings: () -> Unit,
    onNavigateProfiles: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val stats           by viewModel.stats.collectAsState()
    val profile         by viewModel.activeProfile.collectAsState()
    val serviceReady    by viewModel.isServiceReady.collectAsState()

    DisposableEffect(Unit) {
        viewModel.bindService()
        onDispose { viewModel.unbindService() }
    }

    // MediaProjection result
    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.setIntentResult(result.resultCode, result.data!!)
            viewModel.startStream(profile)
        }
    }

    // Audio permission → trigger projection
    val audioPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchProjection(viewModel, projectionLauncher)
    }

    // Notification permission (Android 13+)
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signal Screencaster") },
                actions = {
                    IconButton(onClick = onNavigateProfiles) {
                        Icon(Icons.Outlined.AccountTree, contentDescription = "Profiles")
                    }
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            StreamButton(
                state = connectionState,
                onClick = {
                    when (connectionState) {
                        is ConnectionState.Connected, is ConnectionState.Connecting -> viewModel.stopStream()
                        else -> audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            ConnectionStateChip(connectionState)

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(
                visible = connectionState is ConnectionState.Connected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                StatsBadge(stats = stats)
            }

            Spacer(Modifier.weight(1f))

            // URL preview
            val urlPreview = when (profile.connection.protocol) {
                Protocol.RTMP -> profile.connection.rtmpUrl.ifBlank { "Set stream URL in settings" }
                Protocol.SRT  -> profile.connection.srtUrl.ifBlank  { "Set stream URL in settings" }
            }
            Text(
                text = urlPreview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConnectionStateChip(state: ConnectionState) {
    val label = when (state) {
        is ConnectionState.Idle         -> "Ready"
        is ConnectionState.Connecting   -> "Connecting…"
        is ConnectionState.Connected    -> "● LIVE"
        is ConnectionState.Disconnected -> "Disconnected"
        is ConnectionState.Error        -> "Error"
    }
    val color = when (state) {
        is ConnectionState.Connected    -> MaterialTheme.colorScheme.errorContainer
        is ConnectionState.Connecting   -> MaterialTheme.colorScheme.tertiaryContainer
        is ConnectionState.Error        -> MaterialTheme.colorScheme.errorContainer
        else                            -> MaterialTheme.colorScheme.surfaceVariant
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = color)
    )
}

private fun launchProjection(
    viewModel: HomeViewModel,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    viewModel.startForegroundService()
    // Give the service a moment to start foreground, then get capture intent
    val captureIntent = viewModel.getScreenCaptureIntent()
    if (captureIntent != null) {
        launcher.launch(captureIntent)
    }
}
