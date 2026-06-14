package com.castIRL.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.castIRL.ui.icons.PhosphorIcons
import com.castIRL.ui.theme.MotionTokens

private data class BottomDest(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    BottomDest(Routes.HOME,      "Live",     PhosphorIcons.Broadcast),
    BottomDest(Routes.PROFILES,  "Profiles", PhosphorIcons.FolderOpen),
    BottomDest(Routes.SETTINGS,  "Settings", PhosphorIcons.GearSix),
    BottomDest(Routes.CHANGELOG, "Updates",  PhosphorIcons.ClockCounterClockwise),
)

/** Floating, pill-shaped, icon-only navigation bar. */
@Composable
fun CastIRLBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 10.dp),
    ) {
        Surface(
            shape           = CircleShape,
            color           = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation  = 6.dp,
            modifier        = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                destinations.forEach { dest ->
                    NavItem(
                        icon     = dest.icon,
                        label    = dest.label,
                        selected = currentRoute == dest.route,
                        onClick  = { if (currentRoute != dest.route) onNavigate(dest.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        MotionTokens.effectsDefault(),
        label = "navBg",
    )
    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        MotionTokens.effectsDefault(),
        label = "navTint",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(bg)
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
    }
}
