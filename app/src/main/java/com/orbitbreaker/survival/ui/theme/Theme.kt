package com.orbitbreaker.survival.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OrbitBreakerColorScheme = darkColorScheme(
    primary = PlasmaTurquoise,
    onPrimary = ObsidianBlack,
    primaryContainer = OrbitGreen,
    onPrimaryContainer = StarlightMint,
    secondary = OrbitGreen,
    onSecondary = StarlightMint,
    tertiary = AuroraWarning,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = StarlightMint,
    surface = DeepForest,
    onSurface = StarlightMint,
    surfaceVariant = SurfacePanel,
    onSurfaceVariant = MutedText,
    error = ImpactCoral,
    onError = StarlightMint
)

@Composable
fun OrbitBreakerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OrbitBreakerColorScheme,
        typography = Typography,
        content = content
    )
}