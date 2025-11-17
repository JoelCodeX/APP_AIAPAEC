package com.jotadev.aiapaec.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// TEMA CLARO AIAPAEC
private val AiapaecLightColorScheme = lightColorScheme(
    primary = Crimson40,
    onPrimary = White,

    secondary = Gold100,
    onSecondary = Black,

    tertiary = Crimson60,
    onTertiary = White,

    background = Gray100,
    onBackground = Gray900,

    surface = White,
    onSurface = Gray900,

    surfaceVariant = Gold60,
    onSurfaceVariant = Black,

    error = Color(0xFFFF0000),
    onError = White
)

// TEMA OSCURO AIAPAEC
private val AiapaecDarkColorScheme = darkColorScheme(
    primary = Crimson40,
    onPrimary = White,

    secondary = Gold100,
    onSecondary = Black,

    tertiary = Crimson60,
    onTertiary = White,

    background = Gray100,
    onBackground = Gray900,

    surface = White,
    onSurface = Gray900,

    surfaceVariant = Gold60,
    onSurfaceVariant = Black,

    error = Color(0xFFBA1A1A),
    onError = White
)

@Composable
fun AIAPAECTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        AiapaecDarkColorScheme
    } else {
        AiapaecLightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}