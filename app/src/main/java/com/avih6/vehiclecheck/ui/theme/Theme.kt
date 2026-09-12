package com.avih6.vehiclecheck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Colors
val PrimaryBlue = Color(0xFF00629E)
val OnPrimaryBlue = Color(0xFFFFFFFF)
val PrimaryBlueContainer = Color(0xFFCFE5FF)
val OnPrimaryBlueContainer = Color(0xFF001D34)

val SecondaryTeal = Color(0xFF006A60)
val SecondaryTealContainer = Color(0xFF70F7E5)

val BackgroundLight = Color(0xFFF7F9FC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE2E8F0)

// Dark Colors
val PrimaryBlueDark = Color(0xFF99CBFF)
val OnPrimaryBlueDark = Color(0xFF003355)
val PrimaryBlueContainerDark = Color(0xFF004A79)
val OnPrimaryBlueContainerDark = Color(0xFFCFE5FF)

val BackgroundDark = Color(0xFF0B131E)
val SurfaceDark = Color(0xFF131D2A)
val SurfaceVariantDark = Color(0xFF1E2B3C)

// Vehicle & Test Specific Semantic Colors
val TestValidGreen = Color(0xFF10B981)
val TestExpiringSoonAmber = Color(0xFFF59E0B)
val TestExpiredRed = Color(0xFFEF4444)
val GlassDark = Color(0x33000000)
val GlassWhite = Color(0x1AFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryBlue,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = OnPrimaryBlueContainer,
    secondary = SecondaryTeal,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SecondaryTealContainer,
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBAE6FD),
    onTertiaryContainer = Color(0xFF001E2E),
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = OnPrimaryBlueDark,
    primaryContainer = PrimaryBlueContainerDark,
    onPrimaryContainer = OnPrimaryBlueContainerDark,
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF00354E),
    tertiaryContainer = Color(0xFF075985),
    onTertiaryContainer = Color(0xFFE0F2FE),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFEF4444),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF0F172A)
)

@Composable
fun VehicleCheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}