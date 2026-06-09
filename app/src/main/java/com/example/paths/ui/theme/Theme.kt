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
    lightColorScheme(primary = PurpleLightPrimary, secondary = PurpleLightSecondary, tertiary = PurpleLightTertiary),
    lightColorScheme(primary = GreenLightPrimary, secondary = GreenLightSecondary, tertiary = GreenLightTertiary),
    lightColorScheme(primary = PinkLightPrimary, secondary = PinkLightSecondary, tertiary = PinkLightTertiary),
    lightColorScheme(primary = BlueLightPrimary, secondary = BlueLightSecondary, tertiary = BlueLightTertiary),
    lightColorScheme(primary = OrangeLightPrimary, secondary = OrangeLightSecondary, tertiary = OrangeLightTertiary)
)

// Tablica schematów dla trybu ciemnego
private val DarkSchemes = listOf(
    darkColorScheme(primary = PurpleDarkPrimary, secondary = PurpleDarkSecondary, tertiary = PurpleDarkTertiary),
    darkColorScheme(primary = GreenDarkPrimary, secondary = GreenDarkSecondary, tertiary = GreenDarkTertiary),
    darkColorScheme(primary = PinkDarkPrimary, secondary = PinkDarkSecondary, tertiary = PinkDarkTertiary),
    darkColorScheme(primary = BlueDarkPrimary, secondary = BlueDarkSecondary, tertiary = BlueDarkTertiary),
    darkColorScheme(primary = OrangeDarkPrimary, secondary = OrangeDarkSecondary, tertiary = OrangeDarkTertiary)
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
