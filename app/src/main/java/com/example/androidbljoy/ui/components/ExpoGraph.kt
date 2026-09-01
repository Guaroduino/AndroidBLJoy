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
import com.example.androidbljoy.theme.CyberSecondary
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.NeonGreen

@Composable
fun ExpoGraph(
    expoPercent: Int,
    currentInput: Float = 0f,
    modifier: Modifier = Modifier
        .width(130.dp)
        .height(100.dp)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, CyberSurfaceVariant, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midX = w / 2f
            val midY = h / 2f
            val expo = expoPercent / 100f

            // Grid lines
            drawLine(
                color = CyberSurfaceVariant.copy(alpha = 0.5f),
                start = Offset(0f, midY),
                end = Offset(w, midY),
                strokeWidth = 1f
            )
            drawLine(
                color = CyberSurfaceVariant.copy(alpha = 0.5f),
                start = Offset(midX, 0f),
                end = Offset(midX, h),
                strokeWidth = 1f
            )

            // Linear reference dotted/faint diagonal
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, h),
                end = Offset(w, 0f),
                strokeWidth = 1f
            )

            // Draw Expo Curve: y = (1 - expo)*x + expo*x^3
            val curvePath = Path()
            val steps = 40
            for (i in 0..steps) {
                // x goes from -1.0 to +1.0
                val normX = (i / steps.toFloat()) * 2f - 1f
                val normY = (1f - expo) * normX + expo * (normX * normX * normX)

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
                color = CyberPrimary,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw current stick input dot on the curve
            val stickClamped = currentInput.coerceIn(-1f, 1f)
            val stickOutput = (1f - expo) * stickClamped + expo * (stickClamped * stickClamped * stickClamped)
            val dotX = midX + stickClamped * (w / 2f)
            val dotY = midY - stickOutput * (h / 2f)

            drawCircle(
                color = NeonGreen,
                radius = 4.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }
    }
}
