package com.example.androidbljoy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.LcdBezelOuter
import com.example.androidbljoy.theme.LcdInk
import com.example.androidbljoy.theme.LcdInkSecondary
import com.example.androidbljoy.theme.LcdScreenBg
import com.example.androidbljoy.theme.NeonAmber
import com.example.androidbljoy.theme.NeonGreen
import com.example.androidbljoy.theme.isCarbonLcdTheme

@Composable
fun ExpoGraph(
    expoPercent: Int,
    currentInput: Float = 0f,
    modifier: Modifier = Modifier
        .width(130.dp)
        .height(100.dp)
) {
    val lcdMode = isCarbonLcdTheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (lcdMode) LcdScreenBg else Color.Black.copy(alpha = 0.6f))
            .border(
                width = if (lcdMode) 2.5.dp else 1.dp,
                color = if (lcdMode) LcdBezelOuter else CyberSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midX = w / 2f
            val midY = h / 2f

            // Grid lines
            val gridColor = if (lcdMode) LcdInkSecondary.copy(alpha = 0.45f) else CyberSurfaceVariant.copy(alpha = 0.5f)
            drawLine(
                color = gridColor,
                start = Offset(0f, midY),
                end = Offset(w, midY),
                strokeWidth = 1f
            )
            drawLine(
                color = gridColor,
                start = Offset(midX, 0f),
                end = Offset(midX, h),
                strokeWidth = 1f
            )

            // Linear reference dotted/faint diagonal
            drawLine(
                color = if (lcdMode) LcdInkSecondary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f),
                start = Offset(0f, h),
                end = Offset(w, 0f),
                strokeWidth = 1f
            )

            // Draw Expo Curve
            val curvePath = Path()
            val steps = 40
            for (i in 0..steps) {
                // x goes from -1.0 to +1.0
                val normX = (i / steps.toFloat()) * 2f - 1f
                val normY = calculateExpo(normX, expoPercent)

                // Map normX (-1..1) to screen px (0..w)
                val px = midX + normX * (w / 2f)
                // Invert Y because screen Y is down, but positive math Y is up
                val py = midY - normY * (h / 2f)

                if (i == 0) {
                    curvePath.moveTo(px, py)
                } else {
                    curvePath.lineTo(px, py)
                }
            }

            drawPath(
                path = curvePath,
                color = if (lcdMode) LcdInk else if (expoPercent < 0) NeonAmber else CyberPrimary,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw current stick input dot on the curve
            val stickClamped = currentInput.coerceIn(-1f, 1f)
            val stickOutput = calculateExpo(stickClamped, expoPercent)
            val dotX = midX + stickClamped * (w / 2f)
            val dotY = midY - stickOutput * (h / 2f)

            drawCircle(
                color = if (lcdMode) LcdInk else NeonGreen,
                radius = 4.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }
    }
}

/**
 * Calculates exponential response curve for standard RC transmitters:
 * - expoPercent > 0: Soft / dull center, aggressive at full deflection (precision maneuvers)
 * - expoPercent == 0: Pure linear response
 * - expoPercent < 0: Fast / hyper-reactive center, flattening at full deflection (aggressive maneuvers)
 */
fun calculateExpo(normX: Float, expoPercent: Int): Float {
    val x = normX.coerceIn(-1f, 1f)
    if (expoPercent == 0) return x
    return if (expoPercent > 0) {
        val e = expoPercent / 100f
        (1f - e) * x + e * (x * x * x)
    } else {
        val k = -expoPercent / 100f
        val absX = kotlin.math.abs(x)
        val sign = if (x >= 0f) 1f else -1f
        val convex = 1f - (1f - absX) * (1f - absX) * (1f - absX)
        sign * ((1f - k) * absX + k * convex)
    }
}
