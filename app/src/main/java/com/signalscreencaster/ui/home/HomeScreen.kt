package com.castIRL.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.castIRL.streaming.ConnectionState
import com.castIRL.ui.components.StatsBadge
import com.castIRL.ui.components.StreamButton
import com.castIRL.ui.theme.MotionTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val stats           by viewModel.stats.collectAsState()
    val profile         by viewModel.activeProfile.collectAsState()
    val serviceReady    by viewModel.isServiceReady.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.bindService()
        onDispose { viewModel.unbindService() }
    }

    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startStreamingSession(result.resultCode, result.data!!, profile)
        }
    }

    val audioPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) launchProjection(viewModel, projectionLauncher)
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* proceed regardless */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Error) {
            val reason = (connectionState as ConnectionState.Error).reason
            if (reason.isNotBlank()) snackbarHostState.showSnackbar(reason)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } },
        topBar = {
            TopAppBar(title = { Text("CastIRL", style = MaterialTheme.typography.titleLarge) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 24.dp, end = 24.dp, bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            StreamButton(
                state   = connectionState,
                enabled = serviceReady,
                onClick = {
                    when (connectionState) {
                        is ConnectionState.Connected, is ConnectionState.Connecting ->
                            viewModel.stopStream()
                        else -> {
                            val error = viewModel.urlValidationError(profile)
                            if (error != null) {
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            } else {
                                audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                },
            )

            Spacer(Modifier.height(28.dp))

            ConnectionStateChip(connectionState)

            AnimatedVisibility(
                visible = connectionState is ConnectionState.Connected,
                enter   = scaleIn(MotionTokens.spatialDefault(), initialScale = 0.85f) +
                          expandVertically(MotionTokens.spatialDefault()) +
                          fadeIn(tween(160)),
                exit    = scaleOut(targetScale = 0.9f) +
                          shrinkVertically(tween(200)) +
                          fadeOut(tween(120)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(Modifier.height(36.dp))
                    StatsBadge(stats = stats)
                }
            }
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
    val chipColor by animateColorAsState(
        targetValue   = when (state) {
            is ConnectionState.Connected  -> MaterialTheme.colorScheme.errorContainer
            is ConnectionState.Connecting -> MaterialTheme.colorScheme.tertiaryContainer
            is ConnectionState.Error      -> MaterialTheme.colorScheme.errorContainer
            else                          -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = MotionTokens.effectsDefault(),
        label         = "chipColor",
    )

    SuggestionChip(
        onClick = {},
        label   = {
            AnimatedContent(
                targetState    = label,
                transitionSpec = {
                    (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), initialScale = 0.75f) +
                        fadeIn(tween(120))) togetherWith
                        (scaleOut(targetScale = 0.85f) + fadeOut(tween(80)))
                },
                label = "stateLabel",
            ) { lbl -> Text(lbl) }
        },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = chipColor),
    )
}

private fun launchProjection(
    viewModel: HomeViewModel,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
) {
    val captureIntent = viewModel.getScreenCaptureIntent() ?: return
    launcher.launch(captureIntent)
}
