package com.applymate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = VividOrange,
    onPrimary = Color.White,
    primaryContainer = LightOrange,
    onPrimaryContainer = MutedTerracotta,
    secondary = MutedTerracotta,
    onSecondary = Color.White,
    background = SoftCream,
    onBackground = WarmGreyBrown,
    surface = SoftCream,
    onSurface = WarmGreyBrown,
    surfaceVariant = SoftCreamVariant,
    onSurfaceVariant = DarkGreyBrown,
    outline = WarmGreyBrown
)

@Composable
fun ApplyMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        // You can define a DarkColorScheme here if needed
        LightColorScheme 
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
