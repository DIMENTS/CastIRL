package com.castIRL.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.castIRL.streaming.ConnectionState

@Composable
fun StreamButton(
    state: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLive       = state is ConnectionState.Connected
    val isConnecting = state is ConnectionState.Connecting

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // M3 Expressive: press squish — fast spring in, bouncy spring out
    val pressScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.90f else 1f,
        animationSpec = if (isPressed)
            spring(dampingRatio = Spring.DampingRatioNoBouncy,  stiffness = Spring.StiffnessHigh)
        else
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "pressSquish"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "stream_anim")

    // Live pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = if (isLive) 1.06f else 1f,
        animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
        label         = "pulseScale"
    )

    // Ripple ring 1
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.65f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label         = "ring1Scale"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue  = 0.38f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label         = "ring1Alpha"
    )

    // Ripple ring 2 — offset 700 ms
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.65f,
        animationSpec = infiniteRepeatable(
            tween(1400, delayMillis = 700, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "ring2Scale"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue  = 0.38f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            tween(1400, delayMillis = 700, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )

    val targetColor = when (state) {
        is ConnectionState.Connected    -> MaterialTheme.colorScheme.error
        is ConnectionState.Connecting   -> MaterialTheme.colorScheme.tertiary
        is ConnectionState.Error        -> MaterialTheme.colorScheme.errorContainer
        is ConnectionState.Disconnected -> MaterialTheme.colorScheme.surfaceVariant
        is ConnectionState.Idle         -> MaterialTheme.colorScheme.primaryContainer
    }
    val containerColor by animateColorAsState(
        targetValue   = targetColor,
        animationSpec = tween(350),
        label         = "buttonColor"
    )

    val ringColor = MaterialTheme.colorScheme.error

    val label = when (state) {
        is ConnectionState.Connected    -> "STOP"
        is ConnectionState.Connecting   -> "STOP"
        is ConnectionState.Error        -> "RETRY"
        is ConnectionState.Disconnected -> "GO LIVE"
        is ConnectionState.Idle         -> "GO LIVE"
    }

    val buttonScale = (if (isLive) pulse else 1f) * pressScale

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Ripple rings — live state only
        if (isLive) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(ring1Scale)
                    .drawBehind {
                        drawCircle(color = ringColor.copy(alpha = ring1Alpha))
                    }
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(ring2Scale)
                    .drawBehind {
                        drawCircle(color = ringColor.copy(alpha = ring2Alpha))
                    }
            )
        }

        // Connecting spinner
        if (isConnecting) {
            CircularProgressIndicator(
                modifier    = Modifier.size(164.dp),
                color       = MaterialTheme.colorScheme.tertiary,
                strokeWidth = 3.dp
            )
        }

        FilledTonalButton(
            onClick           = onClick,
            interactionSource = interactionSource,
            modifier          = Modifier
                .size(140.dp)
                .scale(buttonScale),
            shape  = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = containerColor)
        ) {
            // M3 Expressive: label springs in with bounce when state changes
            AnimatedContent(
                targetState    = label,
                transitionSpec = {
                    (scaleIn(
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMedium
                        ),
                        initialScale = 0.72f
                    ) + fadeIn(tween(140))) togetherWith
                    (scaleOut(targetScale = 0.72f) + fadeOut(tween(80)))
                },
                label = "labelTransition"
            ) { targetLabel ->
                Text(targetLabel, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
