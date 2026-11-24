package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val VideoInstaDarkScheme = darkColorScheme(
    primary = CoralPulseDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C0D22),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = AmberFlare,
    onSecondary = Color(0xFF1F1200),
    secondaryContainer = Color(0xFF3E2600),
    onSecondaryContainer = Color(0xFFFFDEA8),
    tertiary = VerifiedEmerald,
    onTertiary = Color(0xFF002114),
    tertiaryContainer = VerifiedEmeraldContainerDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = BlockedCrimson,
    onError = Color.White,
    errorContainer = BlockedCrimsonContainerDark,
    onErrorContainer = Color(0xFFFECACA),
    background = ObsidianBg,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = ObsidianTextSecondary,
    outline = ObsidianOutline,
    outlineVariant = Color(0xFF1E2433)
)

private val VideoInstaLightScheme = lightColorScheme(
    primary = CoralPulse,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4EC),
    onPrimaryContainer = Color(0xFF5D0020),
    secondary = AmberFlare,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3DC),
    onSecondaryContainer = Color(0xFF422800),
    tertiary = VerifiedEmerald,
    onTertiary = Color.White,
    tertiaryContainer = VerifiedEmeraldContainerLight,
    onTertiaryContainer = Color(0xFF064E3B),
    error = BlockedCrimson,
    onError = Color.White,
    errorContainer = BlockedCrimsonContainerLight,
    onErrorContainer = Color(0xFF7F1D1D),
    background = AlabasterBg,
    onBackground = AlabasterTextPrimary,
    surface = AlabasterSurface,
    onSurface = AlabasterTextPrimary,
    surfaceVariant = AlabasterSurfaceElevated,
    onSurfaceVariant = AlabasterTextSecondary,
    outline = AlabasterOutline,
    outlineVariant = Color(0xFFECEAE4)
)

val VideoInstaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun VideoInstaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VideoInstaDarkScheme else VideoInstaLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VideoInstaTypography,
        shapes = VideoInstaShapes,
        content = content
    )
}
