package com.example.androidbljoy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.isCarbonLcdTheme

/**
 * Draws a realistic 2x2 diagonal carbon-fiber weave texture when CARBON_LCD theme is active.
 */
fun Modifier.carbonFiberBackground(): Modifier = this.drawBehind {
    if (!isCarbonLcdTheme) return@drawBehind

    // Base deep carbon resin coat
    drawRect(color = Color(0xFF131518))

    val step = 7.dp.toPx()
    val w = size.width
    val h = size.height
    val maxDim = w + h

    val darkFiber = Color(0xFF0C0E10)
    val midFiber = Color(0xFF1D2026)
    val highlightFiber = Color(0xFF282C34)

    var index = 0
    var d = -h
    while (d < maxDim) {
        val color = when (index % 4) {
            0 -> darkFiber
            1 -> midFiber
            2 -> highlightFiber
            else -> midFiber
        }
        drawLine(
            color = color,
            start = Offset(d, 0f),
            end = Offset(d + h, h),
            strokeWidth = step * 0.72f
        )
        d += step
        index++
    }

    // Subtle counter-diagonal cross-weave shadow lines for authentic 3D twill depth
    val crossStep = step * 2f
    var cd = 0f
    while (cd < maxDim) {
        drawLine(
            color = Color.Black.copy(alpha = 0.28f),
            start = Offset(cd, 0f),
            end = Offset(cd - h, h),
            strokeWidth = 1.2.dp.toPx()
        )
        cd += crossStep
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp, 20.dp)
            .clip(CircleShape)
            .background(if (checked) CyberPrimary else CyberSurfaceVariant)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (checked) Color.Black else MutedText)
        )
    }
}
