// Theme.kt (versión optimizada)
package com.jotadev.aiapaec.ui.theme

import android.app.Activity
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
// TEMA CLARO AIAPAEC - Optimizado
private val AiapaecLightColorScheme = lightColorScheme(
    primary = Crimson40,       // Color secundario
    onPrimary = White,    // Texto sobre secondary

    secondary = Gold100,           // Color principal
    onSecondary = Black,           // Texto sobre primary

    tertiary = Crimson60,        // Color terciario
    onTertiary = White,          // Texto sobre tertiary

    background = Color(0xFFFFFBFE), // Fondo de la app
    onBackground = Gray900,      // Texto sobre fondo

    surface = Color(0xFFFFFBFE), // Superficies (cards, sheets)
    onSurface = Gray900,         // Texto sobre superficie

    surfaceVariant = Gold60,     // Variante de superficie
    onSurfaceVariant = Black,    // Texto sobre surfaceVariant

    error = Color(0xFFBA1A1A),   // Color para errores
    onError = White              // Texto sobre error
)

// TEMA OSCURO AIAPAEC - Optimizado
private val AiapaecDarkColorScheme = darkColorScheme(
    primary = Crimson60,
    onPrimary = White,

    secondary = Gold80,
    onSecondary = Black,

    tertiary = Crimson40,
    onTertiary = White,

    background = Gray900,
    onBackground = White,

    surface = Gray800,
    onSurface = White,

    surfaceVariant = Crimson100,
    onSurfaceVariant = White,

    error = Color(0xFFFFB4AB),
    onError = Black
)

@Composable
fun AIAPAECTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Cambiado a false para usar siempre colores corporativos
    content: @Composable () -> Unit
) {
    // Siempre usar los colores corporativos AIAPAEC, ignorando colores dinámicos
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