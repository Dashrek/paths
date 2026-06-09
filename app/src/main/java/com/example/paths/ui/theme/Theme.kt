package com.example.paths.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Tablica schematów dla trybu jasnego
private val LightSchemes = listOf(
    lightColorScheme(
        primary = PurpleLightPrimary,
        secondary = PurpleLightSecondary,
        tertiary = PurpleLightTertiary,
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE)
    ),
    lightColorScheme(
        primary = GreenLightPrimary,
        secondary = GreenLightSecondary,
        tertiary = GreenLightTertiary,
        background = Color(0xFFF4FFF8),
        surface = Color(0xFFF4FFF8)
    ),
    lightColorScheme(
        primary = PinkLightPrimary,
        secondary = PinkLightSecondary,
        tertiary = PinkLightTertiary,
        background = Color(0xFFFFF0F5),
        surface = Color(0xFFFFF0F5)
    ),
    lightColorScheme(
        primary = BlueLightPrimary,
        secondary = BlueLightSecondary,
        tertiary = BlueLightTertiary,
        background = Color(0xFFF0F8FF),
        surface = Color(0xFFF0F8FF)
    ),
    lightColorScheme(
        primary = OrangeLightPrimary,
        secondary = OrangeLightSecondary,
        tertiary = OrangeLightTertiary,
        background = Color(0xFFFFF5E6),
        surface = Color(0xFFFFF5E6)
    )
)

// Tablica schematów dla trybu ciemnego
private val DarkSchemes = listOf(
    darkColorScheme(
        primary = PurpleDarkPrimary,
        secondary = PurpleDarkSecondary,
        tertiary = PurpleDarkTertiary,
        background = Color(0xFF1C1B1F),
        surface = Color(0xFF1C1B1F)
    ),
    darkColorScheme(
        primary = GreenDarkPrimary,
        secondary = GreenDarkSecondary,
        tertiary = GreenDarkTertiary,
        background = Color(0xFF001F15),
        surface = Color(0xFF001F15)
    ),
    darkColorScheme(
        primary = PinkDarkPrimary,
        secondary = PinkDarkSecondary,
        tertiary = PinkDarkTertiary,
        background = Color(0xFF2D161B),
        surface = Color(0xFF2D161B)
    ),
    darkColorScheme(
        primary = BlueDarkPrimary,
        secondary = BlueDarkSecondary,
        tertiary = BlueDarkTertiary,
        background = Color(0xFF001D33),
        surface = Color(0xFF001D33)
    ),
    darkColorScheme(
        primary = OrangeDarkPrimary,
        secondary = OrangeDarkSecondary,
        tertiary = OrangeDarkTertiary,
        background = Color(0xFF2B1700),
        surface = Color(0xFF2B1700)
    )
)

@Composable
fun PathsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorSchemeIndex: Int = 0, // Indeks wybranego schematu (0-4)
    content: @Composable () -> Unit
) {
    // Zabezpieczenie przed indeksem poza zakresem
    val safeIndex = colorSchemeIndex.coerceIn(0, LightSchemes.size - 1)
    
    val colorScheme = if (darkTheme) {
        DarkSchemes[safeIndex]
    } else {
        LightSchemes[safeIndex]
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
