package com.example.androidbljoy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.OffWhite
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Joystick(
    name: String,
    modifier: Modifier = Modifier,
    isVerticalOnly: Boolean = false,
    isHorizontalOnly: Boolean = false,
    isDigital: Boolean = false,
    externalX: Float? = null,
    externalY: Float? = null,
    onValueChanged: (x: Float, y: Float) -> Unit
) {
    // Current thumb offset in pixels relative to center
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            color = OffWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(180.dp)
                .background(CyberSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                .border(2.dp, CyberPrimary.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isVerticalOnly, isHorizontalOnly, isDigital) {
                        val outerRadius = size.width / 2f
                        // Reserve some padding for the thumb (approx 28dp -> 30px)
                        val maxDragRadius = outerRadius - 40f

                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                thumbOffset = Offset.Zero
                                onValueChanged(0f, 0f)
                            },
                            onDragCancel = {
                                isDragging = false
                                thumbOffset = Offset.Zero
                                onValueChanged(0f, 0f)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                var newOffset = thumbOffset + dragAmount

                                // Enforce axis locking
                                if (isVerticalOnly) {
                                    newOffset = Offset(0f, newOffset.y)
                                }
                                if (isHorizontalOnly) {
                                    newOffset = Offset(newOffset.x, 0f)
                                }

                                // Clamp position to bounding circle radius
                                val distance = sqrt((newOffset.x * newOffset.x + newOffset.y * newOffset.y).toDouble()).toFloat()
                                if (distance > maxDragRadius) {
                                    val angle = kotlin.math.atan2(newOffset.y.toDouble(), newOffset.x.toDouble())
                                    newOffset = Offset(
                                        (maxDragRadius * cos(angle)).toFloat(),
                                        (maxDragRadius * sin(angle)).toFloat()
                                    )
                                }
                                thumbOffset = newOffset

                                // Calculate values
                                val normalizedY = if (!isHorizontalOnly) {
                                    val ratio = - (thumbOffset.y / maxDragRadius)
                                    ratio.coerceIn(-1f, 1f)
                                } else 0f

                                val normalizedX = if (!isVerticalOnly) {
                                    val ratio = thumbOffset.x / maxDragRadius
                                    if (isDigital) {
                                        when {
                                            ratio > 0.1f -> 1f
                                            ratio < -0.1f -> -1f
                                            else -> 0f
                                        }
                                    } else {
                                        ratio.coerceIn(-1f, 1f)
                                    }
                                } else 0f
                                
                                onValueChanged(normalizedX, normalizedY)
                            }
                        )
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.width / 2f
                val arrowColor = CyberPrimary.copy(alpha = 0.3f)

                // Draw background design grid lines
                drawLine(
                    color = CyberPrimary.copy(alpha = 0.15f),
                    start = Offset(center.x - outerRadius * 0.7f, center.y),
                    end = Offset(center.x + outerRadius * 0.7f, center.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = CyberPrimary.copy(alpha = 0.15f),
                    start = Offset(center.x, center.y - outerRadius * 0.7f),
                    end = Offset(center.x, center.y + outerRadius * 0.7f),
                    strokeWidth = 1f
                )

                // Draw guide arrows
                if (isVerticalOnly) {
                    // Draw Up Arrow
                    val upPath = Path().apply {
                        moveTo(center.x, center.y - outerRadius * 0.6f)
                        lineTo(center.x - 10f, center.y - outerRadius * 0.6f + 12f)
                        lineTo(center.x + 10f, center.y - outerRadius * 0.6f + 12f)
                        close()
                    }
                    drawPath(upPath, arrowColor)

                    // Draw Down Arrow
                    val downPath = Path().apply {
                        moveTo(center.x, center.y + outerRadius * 0.6f)
                        lineTo(center.x - 10f, center.y + outerRadius * 0.6f - 12f)
                        lineTo(center.x + 10f, center.y + outerRadius * 0.6f - 12f)
                        close()
                    }
                    drawPath(downPath, arrowColor)
                }

                if (isHorizontalOnly) {
                    // Draw Left Arrow
                    val leftPath = Path().apply {
                        moveTo(center.x - outerRadius * 0.6f, center.y)
                        lineTo(center.x - outerRadius * 0.6f + 12f, center.y - 10f)
                        lineTo(center.x - outerRadius * 0.6f + 12f, center.y + 10f)
                        close()
                    }
                    drawPath(leftPath, arrowColor)

                    // Draw Right Arrow
                    val rightPath = Path().apply {
                        moveTo(center.x + outerRadius * 0.6f, center.y)
                        lineTo(center.x + outerRadius * 0.6f - 12f, center.y - 10f)
                        lineTo(center.x + outerRadius * 0.6f - 12f, center.y + 10f)
                        close()
                    }
                    drawPath(rightPath, arrowColor)
                }

                // Calculate final visual offset
                val maxDragRadius = outerRadius - 40f
                val activeOffset = if (isDragging) {
                    thumbOffset
                } else {
                    val extX = if (externalX != null) externalX * maxDragRadius else 0f
                    val extY = if (externalY != null) -(externalY) * maxDragRadius else 0f
                    Offset(extX, extY)
                }

                // Draw thumb (the draggable knob)
                val thumbRadius = 25f
                val thumbCenter = center + activeOffset
                
                // Outer glow circle
                drawCircle(
                    color = CyberPrimary.copy(alpha = 0.3f),
                    radius = thumbRadius + 10f,
                    center = thumbCenter
                )

                // Main inner knob
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberPrimary, CyberPrimary.copy(alpha = 0.7f)),
                        center = thumbCenter,
                        radius = thumbRadius
                    ),
                    radius = thumbRadius,
                    center = thumbCenter
                )

                // Knob accent center dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 5f,
                    center = thumbCenter
                )
            }
        }
    }
}
