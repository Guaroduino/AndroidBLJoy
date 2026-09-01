package com.example.androidbljoy.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.androidbljoy.AppTheme

@Composable
fun AndroidBLJoyTheme(
  appTheme: AppTheme = AppTheme.CYBERPUNK,
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Construct darkColorScheme dynamically to evaluate CyberBg, CyberPrimary, etc. getters at runtime during composition
  val dynamicColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBg,
    surface = CyberSurface,
    onBackground = OffWhite,
    onSurface = OffWhite,
    surfaceVariant = CyberSurfaceVariant
  )

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      else -> dynamicColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
