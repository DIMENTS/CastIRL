package com.castIRL.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.castIRL.ui.theme.MotionTokens

/**
 * Single-select choice rendered as one connected segmented control — a pill
 * container with equal-width segments, the selected one filled. Clean and
 * minimal; the selected highlight cross-fades (effects spring, no overshoot).
 */
@Composable
fun <T> SegmentedOptionGroup(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            shape    = CircleShape,
            color    = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(4.dp)) {
                options.forEach { option ->
                    val isSelected = option == selected
                    val bg by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        MotionTokens.effectsDefault(),
                        label = "segBg",
                    )
                    val fg by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        MotionTokens.effectsDefault(),
                        label = "segFg",
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable { if (!isSelected) onSelect(option) }
                            .background(bg)
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text      = optionLabel(option),
                            style     = MaterialTheme.typography.labelLarge,
                            color     = fg,
                            maxLines  = 1,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
