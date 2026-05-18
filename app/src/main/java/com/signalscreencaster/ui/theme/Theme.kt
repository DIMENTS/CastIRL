package com.signalscreencaster.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val FallbackDarkScheme = darkColorScheme(
    primary         = LiveRed,
    onPrimary       = SurfaceDark,
    primaryContainer = LiveRedDim,
    surface         = SurfaceDark,
    surfaceContainer = SurfaceCard,
)

@Composable
fun SignalTheme(content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        FallbackDarkScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
