package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = MyraVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = MyraDarkSurfaceContainer,
    onPrimaryContainer = Color(0xFFEADBFF),
    secondary = MyraVioletSecondary,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF004E5B),
    onSecondaryContainer = Color(0xFFA3EEFF),
    tertiary = MyraVioletTertiary,
    onTertiary = Color.White,
    background = MyraDarkBackground,
    onBackground = MyraDarkOnSurface,
    surface = MyraDarkSurface,
    onSurface = MyraDarkOnSurface,
    surfaceVariant = MyraDarkSurfaceVariant,
    onSurfaceVariant = MyraDarkOnSurfaceVariant,
    surfaceContainer = MyraDarkSurfaceContainer,
    outline = Color(0xFF494559)
)

private val LightColorScheme = lightColorScheme(
    primary = MyraLightPrimary,
    onPrimary = Color.White,
    primaryContainer = MyraLightSurfaceContainer,
    onPrimaryContainer = Color(0xFF28005A),
    secondary = MyraLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEEEFE),
    onSecondaryContainer = Color(0xFF001F29),
    tertiary = MyraLightTertiary,
    onTertiary = Color.White,
    background = MyraLightBackground,
    onBackground = MyraLightOnSurface,
    surface = MyraLightSurface,
    onSurface = MyraLightOnSurface,
    surfaceVariant = MyraLightSurfaceVariant,
    onSurfaceVariant = MyraLightOnSurfaceVariant,
    surfaceContainer = MyraLightSurfaceContainer,
    outline = Color(0xFFC7BFE0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to Myra AI's signature futuristic theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
