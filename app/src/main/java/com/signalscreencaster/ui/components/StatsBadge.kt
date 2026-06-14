package com.castIRL.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.castIRL.streaming.StreamStats
import com.castIRL.ui.theme.MotionTokens
import com.castIRL.util.FormatUtil

@Composable
fun StatsBadge(stats: StreamStats, modifier: Modifier = Modifier) {
    val dropColor by animateColorAsState(
        targetValue = if (stats.droppedFrames > 0) MaterialTheme.colorScheme.error
                      else MaterialTheme.colorScheme.onSurface,
        animationSpec = MotionTokens.effectsDefault(),
        label = "dropColor",
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Metric("%.1f".format(stats.bitrateBps / 1_000_000.0), "MBPS")
        Metric(stats.fps.toString(), "FPS")
        Metric(stats.droppedFrames.toString(), "DROPPED", dropColor)
        Metric(FormatUtil.formatDuration(stats.durationMs), "UPTIME")
    }
}

@Composable
private fun Metric(value: String, label: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = valueColor,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
