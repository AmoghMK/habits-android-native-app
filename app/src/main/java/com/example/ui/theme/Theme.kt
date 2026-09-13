package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Emerald500,
    onPrimary = Slate900,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    error = Red400,
    errorContainer = Red900,
    onErrorContainer = Red100
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Emerald500,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald800,
    background = Slate50,
    onBackground = Slate900,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    error = Red500,
    errorContainer = Red100,
    onErrorContainer = Red900
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is set to false to enforce the premium aesthetic consistently
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
