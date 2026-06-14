package com.castIRL.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

// ─── Fallback colour schemes (pre Android 12) ────────────────────────────────

private val FallbackDarkScheme = darkColorScheme(
    primary              = DarkPrimary,
    onPrimary            = DarkOnPrimary,
    primaryContainer     = DarkPrimaryContainer,
    onPrimaryContainer   = DarkOnPrimaryContainer,
    secondary            = DarkSecondary,
    onSecondary          = DarkOnSecondary,
    secondaryContainer   = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary             = DarkTertiary,
    onTertiary           = DarkOnTertiary,
    tertiaryContainer    = DarkTertiaryContainer,
    onTertiaryContainer  = DarkOnTertiaryContainer,
    error                = DarkError,
    onError              = DarkOnError,
    errorContainer       = DarkErrorContainer,
    onErrorContainer     = DarkOnErrorContainer,
    background           = DarkBackground,
    onBackground         = DarkOnBackground,
    surface              = DarkSurface,
    onSurface            = DarkOnSurface,
    surfaceVariant       = DarkSurfaceVariant,
    onSurfaceVariant     = DarkOnSurfaceVariant,
    outline                 = DarkOutline,
    outlineVariant          = DarkOutlineVariant,
    surfaceContainerLowest  = DarkSurfaceContainerLowest,
    surfaceContainerLow     = DarkSurfaceContainerLow,
    surfaceContainer        = DarkSurfaceContainer,
    surfaceContainerHigh    = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceBright           = DarkSurfaceBright,
    surfaceDim              = DarkSurfaceDim,
)

private val FallbackLightScheme = lightColorScheme(
    primary              = LightPrimary,
    onPrimary            = LightOnPrimary,
    primaryContainer     = LightPrimaryContainer,
    onPrimaryContainer   = LightOnPrimaryContainer,
    secondary            = LightSecondary,
    onSecondary          = LightOnSecondary,
    secondaryContainer   = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary             = LightTertiary,
    onTertiary           = LightOnTertiary,
    tertiaryContainer    = LightTertiaryContainer,
    onTertiaryContainer  = LightOnTertiaryContainer,
    error                = LightError,
    onError              = LightOnError,
    errorContainer       = LightErrorContainer,
    onErrorContainer     = LightOnErrorContainer,
    background           = LightBackground,
    onBackground         = LightOnBackground,
    surface              = LightSurface,
    onSurface            = LightOnSurface,
    surfaceVariant       = LightSurfaceVariant,
    onSurfaceVariant     = LightOnSurfaceVariant,
    outline                 = LightOutline,
    outlineVariant          = LightOutlineVariant,
    surfaceContainerLowest  = LightSurfaceContainerLowest,
    surfaceContainerLow     = LightSurfaceContainerLow,
    surfaceContainer        = LightSurfaceContainer,
    surfaceContainerHigh    = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceBright           = LightSurfaceBright,
    surfaceDim              = LightSurfaceDim,
)

// ─── M3 Expressive typography ─────────────────────────────────────────────────
// Emphasises hierarchy with weight contrast and generous sizing.

private val ExpressiveTypography = Typography(
    displayLarge  = TextStyle(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = FontWeight.Bold,     letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold,     letterSpacing = 0.sp),
    displaySmall  = TextStyle(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold,     letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    headlineMedium= TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium,   letterSpacing = 0.sp),
    titleLarge    = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleMedium   = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.15.sp),
    titleSmall    = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,   letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal,   letterSpacing = 0.5.sp),
    bodyMedium    = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal,   letterSpacing = 0.25.sp),
    bodySmall     = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal,   letterSpacing = 0.4.sp),
    labelLarge    = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,   letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,   letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,   letterSpacing = 0.5.sp),
)

// ─── M3 Expressive shapes ─────────────────────────────────────────────────────
// More pronounced rounding — pushes toward the "bubbly" expressive aesthetic.

private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Hero surfaces (GO LIVE panel, live-stats card) sit above the standard scale.
val HeroShape          = RoundedCornerShape(40.dp)
// Morph target for the GO LIVE button press squish.
val ButtonPressedShape = RoundedCornerShape(16.dp)

// ─── Theme entry point ────────────────────────────────────────────────────────

@Composable
fun CastIRLTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> FallbackDarkScheme
        else      -> FallbackLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ExpressiveTypography,
        shapes      = ExpressiveShapes,
        content     = content
    )
}
