package com.example.androidbljoy.theme

import androidx.compose.ui.graphics.Color
import com.example.androidbljoy.AppTheme
import com.example.androidbljoy.MainActivity

val isCarbonLcdTheme: Boolean
    get() = MainActivity.appThemeState.value == AppTheme.CARBON_LCD

val CyberBg: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF0A0D10)
        AppTheme.RETRO_AMBER -> Color(0xFF0C0905)
        AppTheme.CARBON_LCD -> Color(0xFF141619)
    }

val CyberSurface: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF151921)
        AppTheme.RETRO_AMBER -> Color(0xFF1B140B)
        AppTheme.CARBON_LCD -> Color(0xFF1C1F24)
    }

val CyberSurfaceVariant: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF222936)
        AppTheme.RETRO_AMBER -> Color(0xFF2B2012)
        AppTheme.CARBON_LCD -> Color(0xFF2A2F37)
    }

val CyberPrimary: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF00E5FF)
        AppTheme.RETRO_AMBER -> Color(0xFFFFB300) // Amber
        AppTheme.CARBON_LCD -> Color(0xFFD0D7DE)  // Brushed Silver / Titanium
    }

val CyberSecondary: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF00B0FF)
        AppTheme.RETRO_AMBER -> Color(0xFFFF9100) // Orange/Dark Amber
        AppTheme.CARBON_LCD -> Color(0xFF9EA7B3)  // Machined Aluminum
    }

val CyberTertiary: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF8C9EFF)
        AppTheme.RETRO_AMBER -> Color(0xFFFF6D00)
        AppTheme.CARBON_LCD -> Color(0xFF76818E)
    }

val NeonGreen: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF00E676)
        AppTheme.RETRO_AMBER -> Color(0xFFFFC107)
        AppTheme.CARBON_LCD -> Color(0xFF4ADE80)
    }

val NeonRed: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFFFF1744)
        AppTheme.RETRO_AMBER -> Color(0xFFFF3D00)
        AppTheme.CARBON_LCD -> Color(0xFFEF4444)
    }

val NeonAmber: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFFFFB300)
        AppTheme.RETRO_AMBER -> Color(0xFFFFD54F)
        AppTheme.CARBON_LCD -> Color(0xFFF59E0B)
    }

val OffWhite: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFFECEFF1)
        AppTheme.RETRO_AMBER -> Color(0xFFFFECB3)
        AppTheme.CARBON_LCD -> Color(0xFFF1F5F9)
    }

val MutedText: Color
    get() = when (MainActivity.appThemeState.value) {
        AppTheme.CYBERPUNK -> Color(0xFF90A4AE)
        AppTheme.RETRO_AMBER -> Color(0xFFBCAAA4)
        AppTheme.CARBON_LCD -> Color(0xFF94A3B8)
    }

// Classic Monochrome Reflective LCD Palette (Winstar / TX16S Monochrome Display)
val LcdScreenBg = Color(0xFFC8CFC8)
val LcdScreenBgInner = Color(0xFFCFD6CF)
val LcdPixelGrid = Color(0xFFB8C0B8)
val LcdInk = Color(0xFF191E1A)
val LcdInkSecondary = Color(0xFF424B44)
val LcdGhostSegment = Color(0xFFABB3AB)
val LcdBezelOuter = Color(0xFF0B0D0F)
val LcdBezelInner = Color(0xFF282C32)
