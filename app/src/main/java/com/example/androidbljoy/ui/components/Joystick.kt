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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.theme.isCarbonLcdTheme
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
    val carbonMode = isCarbonLcdTheme

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            color = OffWhite,
            fontSize = if (carbonMode) 15.sp else 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    if (carbonMode) Color(0xFF0E1013) else CyberSurfaceVariant.copy(alpha = 0.4f),
                    CircleShape
                )
                .border(
                    width = if (carbonMode) 3.dp else 2.dp,
                    color = if (carbonMode) Color(0xFF2B3038) else CyberPrimary.copy(alpha = 0.6f),
                    shape = CircleShape
                ),
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
                val maxDragRadius = outerRadius - 40f
                val activeOffset = if (isDragging) {
                    thumbOffset
                } else {
                    val extX = if (externalX != null) externalX * maxDragRadius else 0f
                    val extY = if (externalY != null) -(externalY) * maxDragRadius else 0f
                    Offset(extX, extY)
                }
                val thumbCenter = center + activeOffset

                if (carbonMode) {
                    // --- TX16S PHYSICAL GIMBAL RENDERING ---
                    // 1. Matte black outer ring shadow & inner bezel ring
                    drawCircle(
                        color = Color(0xFF171A1F),
                        radius = outerRadius * 0.90f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 2. Outer bezel cardinal white calibration lines (Top, Bottom, Left, Right)
                    val tickColor = Color(0xFFD8DEE6)
                    // Top tick
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x, center.y - outerRadius * 0.88f),
                        end = Offset(center.x, center.y - outerRadius * 0.72f),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Bottom tick
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x, center.y + outerRadius * 0.72f),
                        end = Offset(center.x, center.y + outerRadius * 0.88f),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Left tick
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x - outerRadius * 0.88f, center.y),
                        end = Offset(center.x - outerRadius * 0.72f, center.y),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Right tick
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x + outerRadius * 0.72f, center.y),
                        end = Offset(center.x + outerRadius * 0.88f, center.y),
                        strokeWidth = 2.dp.toPx()
                    )

                    // 3. Inner Rectangular Gimbal Cage with Brushed Silver Frame (like TX16S)
                    val boxW = outerRadius * 1.32f
                    val boxH = outerRadius * 1.08f
                    val boxTopLeft = Offset(center.x - boxW / 2f, center.y - boxH / 2f)

                    // Recessed dark gimbal box
                    drawRoundRect(
                        color = Color(0xFF090A0C),
                        topLeft = boxTopLeft,
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                    // Brushed silver metallic rim around the rectangular gimbal window
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFC4CCD4), Color(0xFF5A626C), Color(0xFFA8B2BC)),
                            start = boxTopLeft,
                            end = Offset(boxTopLeft.x + boxW, boxTopLeft.y + boxH)
                        ),
                        topLeft = boxTopLeft,
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // 4. White Scale Graduation Ticks inside the gimbal window (| | | | |)
                    val scaleTicks = 5
                    val tickSpread = boxW * 0.46f
                    for (i in -scaleTicks..scaleTicks) {
                        val tx = center.x + (i / scaleTicks.toFloat()) * (tickSpread / 2f)
                        val isCenterTick = (i == 0)
                        val tickLen = if (isCenterTick) 7.dp.toPx() else 4.dp.toPx()
                        val tickStroke = if (isCenterTick) 2.dp.toPx() else 1.3.dp.toPx()

                        // Upper row of ticks
                        drawLine(
                            color = tickColor.copy(alpha = 0.85f),
                            start = Offset(tx, center.y - boxH * 0.28f - tickLen),
                            end = Offset(tx, center.y - boxH * 0.28f),
                            strokeWidth = tickStroke
                        )
                        // Lower row of ticks
                        drawLine(
                            color = tickColor.copy(alpha = 0.85f),
                            start = Offset(tx, center.y + boxH * 0.28f),
                            end = Offset(tx, center.y + boxH * 0.28f + tickLen),
                            strokeWidth = tickStroke
                        )
                    }

                    // Horizontal center reference dashes inside gimbal window
                    drawLine(
                        color = tickColor.copy(alpha = 0.9f),
                        start = Offset(center.x - boxW * 0.42f, center.y),
                        end = Offset(center.x - boxW * 0.30f, center.y),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = tickColor.copy(alpha = 0.9f),
                        start = Offset(center.x + boxW * 0.30f, center.y),
                        end = Offset(center.x + boxW * 0.42f, center.y),
                        strokeWidth = 2.dp.toPx()
                    )

                    // 5. Inner Recessed Gimbal Pivot Drum
                    val drumW = boxW * 0.50f
                    val drumH = boxH * 0.44f
                    val drumCenter = center + (activeOffset * 0.28f)
                    drawRoundRect(
                        color = Color(0xFF040506),
                        topLeft = Offset(drumCenter.x - drumW / 2f, drumCenter.y - drumH / 2f),
                        size = Size(drumW, drumH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF22262C),
                        topLeft = Offset(drumCenter.x - drumW / 2f, drumCenter.y - drumH / 2f),
                        size = Size(drumW, drumH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // 6. Stick Shaft (3D bar connecting center pivot to stick head)
                    drawLine(
                        color = Color(0xFF08090B),
                        start = center,
                        end = thumbCenter,
                        strokeWidth = 14.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF3A4048),
                        start = center,
                        end = thumbCenter,
                        strokeWidth = 6.dp.toPx()
                    )

                    // 7. Black Gimbal Stick Collar + Light Grey / Silver Machined Stick Center
                    val outerKnobRadius = 28f
                    val silverCrownRadius = 18f

                    // Drop shadow under stick
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.65f),
                        radius = outerKnobRadius + 5f,
                        center = thumbCenter + Offset(2f, 4f)
                    )

                    // Black outer stick body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2B3036), Color(0xFF0E1013)),
                            center = thumbCenter,
                            radius = outerKnobRadius
                        ),
                        radius = outerKnobRadius,
                        center = thumbCenter
                    )
                    drawCircle(
                        color = Color(0xFF454C56),
                        radius = outerKnobRadius,
                        center = thumbCenter,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Light-Grey / Silver Machined Stick Tip (Resalta en el centro)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFF3F6F9), Color(0xFFC4CCD6), Color(0xFF8E98A4)),
                            center = thumbCenter - Offset(2f, 2f),
                            radius = silverCrownRadius
                        ),
                        radius = silverCrownRadius,
                        center = thumbCenter
                    )

                    // Knurled metallic ring detail on the light-grey tip
                    drawCircle(
                        color = Color(0xFF4A525D),
                        radius = silverCrownRadius * 0.68f,
                        center = thumbCenter,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Dark machined center bore
                    drawCircle(
                        color = Color(0xFF1F242A),
                        radius = 4.5f,
                        center = thumbCenter
                    )
                } else {
                    // --- STANDARD CYBERPUNK / RETRO AMBER RENDERING ---
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
                        val upPath = Path().apply {
                            moveTo(center.x, center.y - outerRadius * 0.6f)
                            lineTo(center.x - 10f, center.y - outerRadius * 0.6f + 12f)
                            lineTo(center.x + 10f, center.y - outerRadius * 0.6f + 12f)
                            close()
                        }
                        drawPath(upPath, arrowColor)

                        val downPath = Path().apply {
                            moveTo(center.x, center.y + outerRadius * 0.6f)
                            lineTo(center.x - 10f, center.y + outerRadius * 0.6f - 12f)
                            lineTo(center.x + 10f, center.y + outerRadius * 0.6f - 12f)
                            close()
                        }
                        drawPath(downPath, arrowColor)
                    }

                    if (isHorizontalOnly) {
                        val leftPath = Path().apply {
                            moveTo(center.x - outerRadius * 0.6f, center.y)
                            lineTo(center.x - outerRadius * 0.6f + 12f, center.y - 10f)
                            lineTo(center.x - outerRadius * 0.6f + 12f, center.y + 10f)
                            close()
                        }
                        drawPath(leftPath, arrowColor)

                        val rightPath = Path().apply {
                            moveTo(center.x + outerRadius * 0.6f, center.y)
                            lineTo(center.x + outerRadius * 0.6f - 12f, center.y - 10f)
                            lineTo(center.x + outerRadius * 0.6f - 12f, center.y + 10f)
                            close()
                        }
                        drawPath(rightPath, arrowColor)
                    }

                    val thumbRadius = 25f

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
}
