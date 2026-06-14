package com.castIRL.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Slider with a Material 3 Expressive wavy active track. The active portion is a
 * flat thick line at rest and animates into a travelling sine wave while dragged.
 * The default pill thumb is kept.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val dragged by interaction.collectIsDraggedAsState()

    val amplitude by animateDpAsState(
        targetValue   = if (dragged) 4.dp else 0.dp,
        animationSpec = tween(250),
        label         = "waveAmp",
    )
    val phase by rememberInfiniteTransition(label = "wavePhase").animateFloat(
        initialValue  = 0f,
        targetValue   = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label         = "phase",
    )

    val activeColor   = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.secondaryContainer

    Slider(
        value                 = value,
        onValueChange         = onValueChange,
        modifier              = modifier,
        valueRange            = valueRange,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource     = interaction,
        thumb = {
            Box(
                Modifier
                    .size(width = 10.dp, height = 40.dp)
                    .background(activeColor, RoundedCornerShape(50)),
            )
        },
        track = {
            WavyTrack(value, valueRange, amplitude, phase, activeColor, inactiveColor)
        },
    )
}

@Composable
private fun WavyTrack(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    amplitude: Dp,
    phase: Float,
    active: Color,
    inactive: Color,
) {
    val span = range.endInclusive - range.start
    val fraction = (if (span > 0f) (value - range.start) / span else 0f).coerceIn(0f, 1f)

    Canvas(Modifier.fillMaxWidth().height(16.dp)) {
        val w = size.width
        val cy = size.height / 2f
        val activeW = fraction * w
        val amp = amplitude.toPx()
        val stroke = 6.dp.toPx()
        val gap = 6.dp.toPx()

        if (activeW + gap < w) {
            drawLine(inactive, Offset(activeW + gap, cy), Offset(w, cy), strokeWidth = stroke, cap = StrokeCap.Round)
        }

        val path = Path().apply { moveTo(0f, cy) }
        val wavelength = 18.dp.toPx()
        var x = 0f
        while (x <= activeW) {
            val y = cy + amp * sin((x / wavelength) * 2f * PI.toFloat() + phase)
            path.lineTo(x, y)
            x += 2f
        }
        drawPath(path, active, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
