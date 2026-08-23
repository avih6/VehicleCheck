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
    secondaryContainer = SecondaryTealContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = OnPrimaryBlueDark,
    primaryContainer = PrimaryBlueContainerDark,
    onPrimaryContainer = OnPrimaryBlueContainerDark,
    secondary = SecondaryTeal,
    secondaryContainer = Color(0xFF005048),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF94A3B8)
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