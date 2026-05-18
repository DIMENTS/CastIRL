package com.signalscreencaster.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.signalscreencaster.streaming.ConnectionState

@Composable
fun StreamButton(
    state: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLive = state is ConnectionState.Connected

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = if (isLive) 1.06f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulseScale"
    )

    val containerColor = when (state) {
        is ConnectionState.Connected    -> MaterialTheme.colorScheme.error
        is ConnectionState.Connecting   -> MaterialTheme.colorScheme.tertiary
        is ConnectionState.Error        -> MaterialTheme.colorScheme.errorContainer
        is ConnectionState.Disconnected -> MaterialTheme.colorScheme.surfaceVariant
        is ConnectionState.Idle         -> MaterialTheme.colorScheme.primaryContainer
    }

    val label = when (state) {
        is ConnectionState.Connected    -> "STOP"
        is ConnectionState.Connecting   -> "STOP"
        is ConnectionState.Error        -> "RETRY"
        is ConnectionState.Disconnected -> "GO LIVE"
        is ConnectionState.Idle         -> "GO LIVE"
    }

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .size(140.dp)
            .scale(if (isLive) pulse else 1f),
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = containerColor)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
