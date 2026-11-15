package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = ElectricIndigoDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = CyanMint,
    onSecondary = ObsidianBg,
    secondaryContainer = Color(0xFF10382F),
    onSecondaryContainer = CyanMint,
    tertiary = CoralSpark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF47182B),
    onTertiaryContainer = CoralSparkLight,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerHigh = ObsidianSurfaceHigh,
    outline = ObsidianBorder,
    outlineVariant = Color(0xFF1F2739),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = Color(0xFFFFB4BE)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricIndigoDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E0FF),
    onPrimaryContainer = Color(0xFF1E0B68),
    secondary = CyanMintDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0FAEE),
    onSecondaryContainer = Color(0xFF003829),
    tertiary = CoralSpark,
    onTertiary = Color.White,
    background = StudioLightBg,
    onBackground = TextPrimaryLight,
    surface = StudioLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = StudioLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = StudioLightBorder,
    outlineVariant = Color(0xFFE5E9F3),
    error = Color(0xFFD92D45),
    onError = Color.White
)

private val StudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun WebForgeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StudioShapes,
        content = content
    )
}
