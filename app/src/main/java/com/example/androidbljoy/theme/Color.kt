package com.example.androidbljoy.theme

import androidx.compose.ui.graphics.Color
import com.example.androidbljoy.MainActivity
import com.example.androidbljoy.AppTheme

val CyberBg: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF0A0D10) else Color(0xFF0C0905)

val CyberSurface: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF151921) else Color(0xFF1B140B)

val CyberSurfaceVariant: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF222936) else Color(0xFF2B2012)

val CyberPrimary: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF00E5FF) else Color(0xFFFFB300) // Amber

val CyberSecondary: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF00B0FF) else Color(0xFFFF9100) // Orange/Dark Amber

val CyberTertiary: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF8C9EFF) else Color(0xFFFF6D00)

val NeonGreen: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF00E676) else Color(0xFFFFC107)

val NeonRed: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFFFF1744) else Color(0xFFFF3D00)

val NeonAmber: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFFFFB300) else Color(0xFFFFD54F)

val OffWhite: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFFECEFF1) else Color(0xFFFFECB3)

val MutedText: Color
    get() = if (MainActivity.appThemeState.value == AppTheme.CYBERPUNK) Color(0xFF90A4AE) else Color(0xFFBCAAA4)
