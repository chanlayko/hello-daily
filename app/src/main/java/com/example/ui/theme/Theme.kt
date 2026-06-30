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

private val DarkColorScheme =
  darkColorScheme(
    primary = PurpDarkPrimary,
    onPrimary = PurpDarkOnPrimary,
    primaryContainer = PurpDarkPrimaryContainer,
    onPrimaryContainer = PurpDarkOnPrimaryContainer,
    secondary = PurpDarkSecondary,
    onSecondary = PurpDarkOnSecondary,
    secondaryContainer = PurpDarkSecondaryContainer,
    onSecondaryContainer = PurpDarkOnSecondaryContainer,
    background = PurpDarkBackground,
    onBackground = PurpDarkOnBackground,
    surface = PurpDarkSurface,
    onSurface = PurpDarkOnSurface,
    surfaceVariant = PurpDarkSurfaceVariant,
    onSurfaceVariant = PurpDarkOnSurfaceVariant,
    outline = PurpOutline,
    outlineVariant = PurpDarkOnSurfaceVariant,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PurpPrimary,
    onPrimary = PurpOnPrimary,
    primaryContainer = PurpPrimaryContainer,
    onPrimaryContainer = PurpOnPrimaryContainer,
    secondary = PurpSecondary,
    onSecondary = PurpOnSecondary,
    secondaryContainer = PurpSecondaryContainer,
    onSecondaryContainer = PurpOnSecondaryContainer,
    background = PurpBackground,
    onBackground = PurpOnBackground,
    surface = PurpSurface,
    onSurface = PurpOnSurface,
    surfaceVariant = PurpSurfaceVariant,
    onSurfaceVariant = PurpOnSurfaceVariant,
    outline = PurpOutline,
    outlineVariant = PurpOutlineVariant,
    error = PurpError,
    onError = PurpOnError,
    errorContainer = PurpErrorContainer,
    onErrorContainer = PurpOnErrorContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disables dynamic coloring to enforce custom High Density design
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
