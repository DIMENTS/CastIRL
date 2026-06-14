package com.castIRL.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
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
import com.castIRL.ui.icons.PhosphorIcons
import com.castIRL.ui.theme.MotionTokens

private val BUTTON_SIZE = 168.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StreamButton(
    state: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val isLive       = state is ConnectionState.Connected
    val isConnecting = state is ConnectionState.Connecting
    val showGlyph    = !isLive && !isConnecting

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press squish — combined with corner morph for the expressive press.
    val pressScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.90f else 1f,
        animationSpec = if (isPressed) MotionTokens.spatialFast() else MotionTokens.spatialDefault(),
        label         = "pressSquish",
    )

    // Corner morph — circle at rest (half of size), rounded-square on press.
    val corner by animateDpAsState(
        targetValue   = if (isPressed) 20.dp else BUTTON_SIZE / 2,
        animationSpec = if (isPressed) MotionTokens.spatialFast() else MotionTokens.spatialDefault(),
        label         = "cornerMorph",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "stream_anim")

    val pulse by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = if (isLive) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
        label         = "pulseScale",
    )

    // Ripple rings — live only
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.65f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "ring1Scale",
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.38f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "ring1Alpha",
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.65f,
        animationSpec = infiniteRepeatable(tween(1400, delayMillis = 700, easing = LinearEasing), RepeatMode.Restart),
        label = "ring2Scale",
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.38f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, delayMillis = 700, easing = LinearEasing), RepeatMode.Restart),
        label = "ring2Alpha",
    )

    val targetContainer = when (state) {
        is ConnectionState.Connected    -> MaterialTheme.colorScheme.error
        is ConnectionState.Connecting   -> MaterialTheme.colorScheme.tertiary
        is ConnectionState.Error        -> MaterialTheme.colorScheme.errorContainer
        is ConnectionState.Disconnected -> MaterialTheme.colorScheme.surfaceVariant
        is ConnectionState.Idle         -> MaterialTheme.colorScheme.primaryContainer
    }
    val targetContent = when (state) {
        is ConnectionState.Connected    -> MaterialTheme.colorScheme.onError
        is ConnectionState.Connecting   -> MaterialTheme.colorScheme.onTertiary
        is ConnectionState.Error        -> MaterialTheme.colorScheme.onErrorContainer
        is ConnectionState.Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant
        is ConnectionState.Idle         -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    // Color SETTLES (effects spring) — never overshoots.
    val containerColor by animateColorAsState(targetContainer, MotionTokens.effectsDefault(), label = "container")
    val contentColor   by animateColorAsState(targetContent,   MotionTokens.effectsDefault(), label = "content")

    val ringColor = MaterialTheme.colorScheme.error

    val label = when (state) {
        is ConnectionState.Connected    -> "STOP"
        is ConnectionState.Connecting   -> "STOP"
        is ConnectionState.Error        -> "RETRY"
        is ConnectionState.Disconnected -> "GO LIVE"
        is ConnectionState.Idle         -> "GO LIVE"
    }

    val buttonScale = (if (isLive) pulse else 1f) * pressScale

    Box(contentAlignment = Alignment.Center, modifier = modifier) {

        if (isLive) {
            Box(
                Modifier.size(BUTTON_SIZE).scale(ring1Scale)
                    .drawBehind { drawCircle(color = ringColor.copy(alpha = ring1Alpha)) },
            )
            Box(
                Modifier.size(BUTTON_SIZE).scale(ring2Scale)
                    .drawBehind { drawCircle(color = ringColor.copy(alpha = ring2Alpha)) },
            )
        }

        FilledTonalButton(
            onClick           = onClick,
            enabled           = enabled,
            interactionSource = interactionSource,
            modifier          = Modifier.size(BUTTON_SIZE).scale(buttonScale),
            shape             = RoundedCornerShape(corner),
            colors            = ButtonDefaults.filledTonalButtonColors(
                containerColor = containerColor,
                contentColor   = contentColor,
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when {
                    isConnecting -> LoadingIndicator(
                        modifier = Modifier.size(32.dp),
                        color    = contentColor,
                    )
                    showGlyph -> Icon(
                        imageVector        = PhosphorIcons.Broadcast,
                        contentDescription = null,
                        modifier           = Modifier.size(34.dp),
                    )
                }
                Spacer(Modifier.height(2.dp))
                AnimatedContent(
                    targetState    = label,
                    transitionSpec = {
                        (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), initialScale = 0.72f) +
                            fadeIn(tween(140))) togetherWith
                            (scaleOut(targetScale = 0.72f) + fadeOut(tween(80)))
                    },
                    label = "labelTransition",
                ) { targetLabel ->
                    Text(targetLabel, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
