package com.castIRL.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.castIRL.streaming.StreamStats
import com.castIRL.util.FormatUtil

@Composable
fun StatsBadge(stats: StreamStats, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StatChip(FormatUtil.formatBitrate(stats.bitrateBps))
        StatChip("${stats.fps} fps")
        if (stats.droppedFrames > 0) StatChip("${stats.droppedFrames} drop")
        StatChip(FormatUtil.formatDuration(stats.durationMs))
    }
}

@Composable
private fun StatChip(label: String) {
    SuggestionChip(
        onClick  = {},
        modifier = Modifier.animateContentSize(
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMediumLow
            )
        ),
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}
