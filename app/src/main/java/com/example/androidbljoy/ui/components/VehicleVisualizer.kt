package com.example.androidbljoy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.LcdBezelInner
import com.example.androidbljoy.theme.LcdBezelOuter
import com.example.androidbljoy.theme.LcdGhostSegment
import com.example.androidbljoy.theme.LcdInk
import com.example.androidbljoy.theme.LcdInkSecondary
import com.example.androidbljoy.theme.LcdPixelGrid
import com.example.androidbljoy.theme.LcdScreenBg
import com.example.androidbljoy.theme.LcdScreenBgInner
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.NeonAmber
import com.example.androidbljoy.theme.NeonGreen
import com.example.androidbljoy.theme.NeonRed
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.theme.isCarbonLcdTheme
import kotlin.math.abs

/**
 * Draws a subtle dot-matrix LCD background grid on monochrome LCD screens.
 */
fun Modifier.lcdDotMatrixBackground(): Modifier = this.drawBehind {
    drawRect(color = LcdScreenBg)
    val step = 4.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = LcdPixelGrid.copy(alpha = 0.45f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
    var x = 0f
    while (x < size.width) {
        drawLine(
            color = LcdPixelGrid.copy(alpha = 0.45f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
    }
}

@Composable
fun VehicleVisualizer(
    vehicleType: DrivingMode,
    motorA: Int,
    motorB: Int,
    servoAngle: Int,
    modifier: Modifier = Modifier
) {
    val lcdMode = isCarbonLcdTheme

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (lcdMode) 4.dp else 1.dp,
                color = if (lcdMode) LcdBezelOuter else CyberPrimary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(if (lcdMode) 10.dp else 12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (lcdMode) LcdBezelInner else CyberSurface.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(if (lcdMode) 10.dp else 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (lcdMode) 4.dp else 0.dp)
                .clip(RoundedCornerShape(if (lcdMode) 6.dp else 12.dp))
                .then(
                    if (lcdMode) {
                        Modifier
                            .lcdDotMatrixBackground()
                            .border(1.5.dp, LcdInk.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Mode Header Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (lcdMode) {
                                Modifier
                                    .background(LcdInk, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            } else {
                                Modifier
                            }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (vehicleType) {
                            DrivingMode.DUAL_DC -> "DOBLE MOTOR DC"
                            DrivingMode.TANK -> "ORUGAS TANQUE"
                            DrivingMode.SERVO_CAR -> "COCHE RC + SERVO"
                            DrivingMode.ARCADE -> "ARCADE DIFERENCIAL"
                        },
                        color = if (lcdMode) LcdScreenBg else CyberPrimary,
                        fontSize = 11.sp,
                        fontFamily = if (lcdMode) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (lcdMode) "LCD TELEM" else "TELEMETRÍA PWM",
                        color = if (lcdMode) LcdScreenBg.copy(alpha = 0.85f) else MutedText,
                        fontSize = 9.sp,
                        fontFamily = if (lcdMode) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Main Graphic Arena
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (vehicleType) {
                        DrivingMode.DUAL_DC -> {
                            // Left Motor Bar (Traction)
                            PwmVerticalBar(
                                label = "TRACCIÓN (A)",
                                value = motorA,
                                modifier = Modifier.width(38.dp).fillMaxHeight()
                            )

                            // Central Car Graphic (Rear Wheels with PWM bars, Front Wheels with discrete turn)
                            DualMotorChassisGraphic(
                                motorA = motorA,
                                motorB = motorB,
                                isArcade = false,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )

                            // Right Directional Bar (Discrete Steering Left / Right)
                            DirectionalMotorBar(
                                label = "DIRECCIÓN (B)",
                                value = motorB,
                                modifier = Modifier.width(38.dp).fillMaxHeight()
                            )
                        }

                        DrivingMode.ARCADE -> {
                            // Left Motor PWM Bar
                            PwmVerticalBar(
                                label = "MTR A (Izq)",
                                value = motorA,
                                modifier = Modifier.width(36.dp).fillMaxHeight()
                            )

                            DualMotorChassisGraphic(
                                motorA = motorA,
                                motorB = motorB,
                                isArcade = true,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )

                            // Right Motor PWM Bar
                            PwmVerticalBar(
                                label = "MTR B (Der)",
                                value = motorB,
                                modifier = Modifier.width(36.dp).fillMaxHeight()
                            )
                        }

                        DrivingMode.TANK -> {
                            // Left Track Bar
                            PwmVerticalBar(
                                label = "ORUGA IZQ",
                                value = motorA,
                                modifier = Modifier.width(36.dp).fillMaxHeight()
                            )

                            // Tank Chassis Graphic
                            TankChassisGraphic(
                                trackLeft = motorA,
                                trackRight = motorB,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )

                            // Right Track Bar
                            PwmVerticalBar(
                                label = "ORUGA DER",
                                value = motorB,
                                modifier = Modifier.width(36.dp).fillMaxHeight()
                            )
                        }

                        DrivingMode.SERVO_CAR -> {
                            // Throttle Motor Bar
                            PwmVerticalBar(
                                label = "TRACCIÓN",
                                value = if (motorA != 0) motorA else motorB,
                                modifier = Modifier.width(36.dp).fillMaxHeight()
                            )

                            // Servo Car with pivoting front wheels
                            ServoCarChassisGraphic(
                                throttle = if (motorA != 0) motorA else motorB,
                                servoAngle = servoAngle,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )

                            // Servo Angle Dial / Bar
                            ServoAngleBar(
                                angle = servoAngle,
                                modifier = Modifier.width(42.dp).fillMaxHeight()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom quick readout numbers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (lcdMode) LcdScreenBgInner else Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(if (lcdMode) 4.dp else 6.dp)
                        )
                        .then(
                            if (lcdMode) Modifier.border(1.dp, LcdInk, RoundedCornerShape(4.dp))
                            else Modifier
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val colorA = if (lcdMode) {
                        if (motorA != 0) LcdInk else LcdInkSecondary
                    } else {
                        if (motorA > 0) NeonGreen else if (motorA < 0) NeonRed else MutedText
                    }
                    val colorB = if (lcdMode) {
                        if (motorB != 0) LcdInk else LcdInkSecondary
                    } else {
                        if (motorB > 0) NeonGreen else if (motorB < 0) NeonRed else MutedText
                    }

                    Text(
                        text = "A: ${if (motorA > 0) "+$motorA" else "$motorA"}",
                        color = colorA,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "B: ${if (motorB > 0) "+$motorB" else "$motorB"}",
                        color = colorB,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (vehicleType == DrivingMode.SERVO_CAR) {
                        Text(
                            text = "SERVO: $servoAngle°",
                            color = if (lcdMode) LcdInk else CyberPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PwmVerticalBar(
    label: String,
    value: Int,
    maxValue: Int = 255,
    modifier: Modifier = Modifier
) {
    val lcdMode = isCarbonLcdTheme
    val clamped = value.coerceIn(-maxValue, maxValue)
    val ratio = abs(clamped) / maxValue.toFloat()
    val isForward = clamped > 0
    val isReverse = clamped < 0

    val barColor = if (lcdMode) {
        if (isForward || isReverse) LcdInk else LcdGhostSegment
    } else {
        when {
            isForward -> NeonGreen
            isReverse -> NeonAmber
            else -> CyberSurfaceVariant
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = if (lcdMode) LcdInk else MutedText,
            fontSize = 7.sp,
            fontFamily = if (lcdMode) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(14.dp)
                .weight(1f)
                .background(
                    if (lcdMode) LcdGhostSegment.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)
                )
                .border(
                    width = if (lcdMode) 1.5.dp else 1.dp,
                    color = if (lcdMode) LcdInk else CyberSurfaceVariant,
                    shape = RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)
                )
                .clip(RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midY = size.height / 2f

                // Center zero line
                drawLine(
                    color = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.7f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = if (lcdMode) 2f else 1.5f
                )

                if (isForward) {
                    val barHeight = midY * ratio
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(0f, midY - barHeight),
                        size = Size(size.width, barHeight),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                } else if (isReverse) {
                    val barHeight = midY * ratio
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(0f, midY),
                        size = Size(size.width, barHeight),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$clamped",
            color = if (lcdMode) LcdInk else if (clamped != 0) barColor else MutedText,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ServoAngleBar(
    angle: Int,
    modifier: Modifier = Modifier
) {
    val lcdMode = isCarbonLcdTheme
    val clamped = angle.coerceIn(0, 180)
    // 90 is center (0 offset), -90 to +90 offset
    val offsetFromCenter = clamped - 90
    val ratio = abs(offsetFromCenter) / 90f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SERVO",
            color = if (lcdMode) LcdInk else MutedText,
            fontSize = 7.sp,
            fontFamily = if (lcdMode) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(14.dp)
                .weight(1f)
                .background(
                    if (lcdMode) LcdGhostSegment.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)
                )
                .border(
                    width = if (lcdMode) 1.5.dp else 1.dp,
                    color = if (lcdMode) LcdInk else CyberSurfaceVariant,
                    shape = RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)
                )
                .clip(RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midY = size.height / 2f
                val activeColor = if (lcdMode) LcdInk else CyberPrimary

                // Center zero line (90°)
                drawLine(
                    color = activeColor,
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 2f
                )

                if (offsetFromCenter != 0) {
                    val barHeight = midY * ratio
                    val top = if (offsetFromCenter > 0) midY - barHeight else midY
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(0f, top),
                        size = Size(size.width, barHeight),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$clamped°",
            color = if (lcdMode) LcdInk else CyberPrimary,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DirectionalMotorBar(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    val lcdMode = isCarbonLcdTheme
    val isLeft = value < -15
    val isRight = value > 15
    val isCenter = !isLeft && !isRight

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = if (lcdMode) LcdInk else MutedText,
            fontSize = 7.sp,
            fontFamily = if (lcdMode) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(24.dp)
                .weight(1f)
                .background(
                    if (lcdMode) LcdGhostSegment.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)
                )
                .border(
                    width = if (lcdMode) 1.5.dp else 1.dp,
                    color = if (lcdMode) LcdInk else CyberSurfaceVariant,
                    shape = RoundedCornerShape(if (lcdMode) 2.dp else 4.dp)
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val activeBg = if (lcdMode) LcdInk else NeonGreen
                val inactiveBg = if (lcdMode) LcdGhostSegment.copy(alpha = 0.5f) else CyberSurfaceVariant.copy(alpha = 0.25f)
                val activeFg = if (lcdMode) LcdScreenBg else Color.Black
                val inactiveFg = if (lcdMode) LcdInkSecondary else MutedText

                // Left Turn Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isLeft) activeBg else inactiveBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◄", color = if (isLeft) activeFg else inactiveFg, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Center Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isCenter) {
                                if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.5f)
                            } else inactiveBg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "●",
                        color = if (isCenter) {
                            if (lcdMode) LcdScreenBg else CyberPrimary
                        } else inactiveFg,
                        fontSize = 7.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Right Turn Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isRight) activeBg else inactiveBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("►", color = if (isRight) activeFg else inactiveFg, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = when {
                isLeft -> "◄ IZQ"
                isRight -> "DER ►"
                else -> "RECTO"
            },
            color = if (lcdMode) LcdInk else if (!isCenter) NeonGreen else MutedText,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DualMotorChassisGraphic(
    motorA: Int,
    motorB: Int,
    isArcade: Boolean,
    modifier: Modifier = Modifier
) {
    val lcdMode = isCarbonLcdTheme

    if (isArcade) {
        // Arcade Differential Chassis
        Box(
            modifier = modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(width = 120.dp, height = 110.dp)) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                // Central Rover Chassis Body
                drawRoundRect(
                    color = if (lcdMode) LcdGhostSegment else CyberSurfaceVariant.copy(alpha = 0.7f),
                    topLeft = Offset(centerX - 24.dp.toPx(), centerY - 38.dp.toPx()),
                    size = Size(48.dp.toPx(), 76.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                drawRoundRect(
                    color = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.6f),
                    topLeft = Offset(centerX - 24.dp.toPx(), centerY - 38.dp.toPx()),
                    size = Size(48.dp.toPx(), 76.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = if (lcdMode) 2.dp.toPx() else 1.5.dp.toPx())
                )

                // Front direction indicator arrow
                val arrowPath = Path().apply {
                    moveTo(centerX, centerY - 28.dp.toPx())
                    lineTo(centerX - 8.dp.toPx(), centerY - 16.dp.toPx())
                    lineTo(centerX + 8.dp.toPx(), centerY - 16.dp.toPx())
                    close()
                }
                drawPath(arrowPath, color = if (lcdMode) LcdInk else CyberPrimary)

                // Left Wheels (Motor A)
                val leftColor = if (lcdMode) {
                    if (motorA != 0) LcdInk else LcdInkSecondary.copy(alpha = 0.55f)
                } else {
                    if (motorA > 0) NeonGreen else if (motorA < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
                }
                drawRoundRect(color = leftColor, topLeft = Offset(centerX - 38.dp.toPx(), centerY - 34.dp.toPx()), size = Size(10.dp.toPx(), 22.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                drawRoundRect(color = leftColor, topLeft = Offset(centerX - 38.dp.toPx(), centerY + 12.dp.toPx()), size = Size(10.dp.toPx(), 22.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))

                // Right Wheels (Motor B)
                val rightColor = if (lcdMode) {
                    if (motorB != 0) LcdInk else LcdInkSecondary.copy(alpha = 0.55f)
                } else {
                    if (motorB > 0) NeonGreen else if (motorB < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
                }
                drawRoundRect(color = rightColor, topLeft = Offset(centerX + 28.dp.toPx(), centerY - 34.dp.toPx()), size = Size(10.dp.toPx(), 22.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                drawRoundRect(color = rightColor, topLeft = Offset(centerX + 28.dp.toPx(), centerY + 12.dp.toPx()), size = Size(10.dp.toPx(), 22.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
            }
        }
    } else {
        // Dual DC Car: Rear wheels have PWM power bars, Front wheels have discrete left/right turn
        val isTurnLeft = motorB < -15
        val isTurnRight = motorB > 15
        val discreteAngle = when {
            isTurnLeft -> -28f
            isTurnRight -> 28f
            else -> 0f
        }
        val animatedAngle by animateFloatAsState(targetValue = discreteAngle, animationSpec = tween(70), label = "dcSteer")

        val tractionColor = if (lcdMode) {
            LcdInk
        } else {
            if (motorA > 0) NeonGreen else if (motorA < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
        }
        val tractionRatio = (abs(motorA).coerceIn(0, 255) / 255f)

        Box(
            modifier = modifier.padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(width = 130.dp, height = 115.dp),
                contentAlignment = Alignment.Center
            ) {
                // Static Hull & Rear Wheels with Live Bars
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // Car Chassis Body
                    drawRoundRect(
                        color = if (lcdMode) LcdGhostSegment else CyberSurfaceVariant.copy(alpha = 0.8f),
                        topLeft = Offset(centerX - 20.dp.toPx(), centerY - 38.dp.toPx()),
                        size = Size(40.dp.toPx(), 74.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    drawRoundRect(
                        color = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.6f),
                        topLeft = Offset(centerX - 20.dp.toPx(), centerY - 38.dp.toPx()),
                        size = Size(40.dp.toPx(), 74.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(width = if (lcdMode) 2.dp.toPx() else 1.5.dp.toPx())
                    )

                    // Axles
                    val axleColor = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.6f)
                    drawLine(
                        color = axleColor,
                        start = Offset(centerX - 30.dp.toPx(), centerY - 24.dp.toPx()),
                        end = Offset(centerX + 30.dp.toPx(), centerY - 24.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = axleColor,
                        start = Offset(centerX - 32.dp.toPx(), centerY + 24.dp.toPx()),
                        end = Offset(centerX + 32.dp.toPx(), centerY + 24.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )

                    // REAR WHEELS WITH PWM LEVEL BARS
                    val rearWheelW = 10.dp.toPx()
                    val rearWheelH = 26.dp.toPx()
                    val leftRearX = centerX - 36.dp.toPx()
                    val rightRearX = centerX + 26.dp.toPx()
                    val rearY = centerY + 11.dp.toPx()

                    val wheelFrameBg = if (lcdMode) LcdGhostSegment else Color.Black.copy(alpha = 0.85f)
                    val wheelFrameStroke = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.5f)

                    // Left Rear Wheel Frame & Bar
                    drawRoundRect(color = wheelFrameBg, topLeft = Offset(leftRearX, rearY), size = Size(rearWheelW, rearWheelH), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                    drawRoundRect(color = wheelFrameStroke, topLeft = Offset(leftRearX, rearY), size = Size(rearWheelW, rearWheelH), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
                    if (tractionRatio > 0f) {
                        val barH = rearWheelH * tractionRatio
                        val barTop = if (motorA > 0) rearY + (rearWheelH - barH) else rearY
                        drawRoundRect(color = tractionColor, topLeft = Offset(leftRearX, barTop), size = Size(rearWheelW, barH), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                    }

                    // Right Rear Wheel Frame & Bar
                    drawRoundRect(color = wheelFrameBg, topLeft = Offset(rightRearX, rearY), size = Size(rearWheelW, rearWheelH), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                    drawRoundRect(color = wheelFrameStroke, topLeft = Offset(rightRearX, rearY), size = Size(rearWheelW, rearWheelH), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
                    if (tractionRatio > 0f) {
                        val barH = rearWheelH * tractionRatio
                        val barTop = if (motorA > 0) rearY + (rearWheelH - barH) else rearY
                        drawRoundRect(color = tractionColor, topLeft = Offset(rightRearX, barTop), size = Size(rearWheelW, barH), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                    }
                }

                // FRONT WHEELS (Rotating Discretely Left or Right - NOT proportional)
                val frontSteerColor = if (lcdMode) {
                    if (isTurnLeft || isTurnRight) LcdInk else LcdInkSecondary
                } else {
                    if (isTurnLeft || isTurnRight) NeonGreen else OffWhite.copy(alpha = 0.4f)
                }

                // Front Left Wheel
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 28.dp, top = 20.dp)
                        .graphicsLayer {
                            rotationZ = animatedAngle
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                        }
                        .size(width = 10.dp, height = 24.dp)
                        .background(frontSteerColor, RoundedCornerShape(3.dp))
                        .border(1.dp, if (lcdMode) LcdInk else Color.Black.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                )

                // Front Right Wheel
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 28.dp, top = 20.dp)
                        .graphicsLayer {
                            rotationZ = animatedAngle
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                        }
                        .size(width = 10.dp, height = 24.dp)
                        .background(frontSteerColor, RoundedCornerShape(3.dp))
                        .border(1.dp, if (lcdMode) LcdInk else Color.Black.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                )

                // Directional Turn Text Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 3.dp)
                ) {
                    Text(
                        text = when {
                            isTurnLeft -> "◄◄ GIRO IZQ"
                            isTurnRight -> "GIRO DER ►►"
                            else -> "▲ RECTO ▲"
                        },
                        color = if (lcdMode) LcdInk else if (isTurnLeft || isTurnRight) NeonGreen else MutedText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Rear Wheels Label
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "RUEDAS TRASERAS PWM",
                        color = if (lcdMode) LcdInkSecondary else MutedText,
                        fontSize = 7.sp,
                        fontFamily = if (lcdMode) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TankChassisGraphic(
    trackLeft: Int,
    trackRight: Int,
    modifier: Modifier = Modifier
) {
    val lcdMode = isCarbonLcdTheme

    Box(
        modifier = modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 120.dp, height = 110.dp)) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f

            // Tank Main Hull
            drawRoundRect(
                color = if (lcdMode) LcdGhostSegment else CyberSurfaceVariant,
                topLeft = Offset(centerX - 20.dp.toPx(), centerY - 34.dp.toPx()),
                size = Size(40.dp.toPx(), 68.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            drawRoundRect(
                color = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.5f),
                topLeft = Offset(centerX - 20.dp.toPx(), centerY - 34.dp.toPx()),
                size = Size(40.dp.toPx(), 68.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = if (lcdMode) 2.dp.toPx() else 1.5.dp.toPx())
            )

            // Turret circle
            drawCircle(
                color = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.8f),
                radius = 12.dp.toPx(),
                center = Offset(centerX, centerY)
            )

            // Cannon Barrel pointing up
            drawLine(
                color = if (lcdMode) LcdInk else CyberPrimary,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - 32.dp.toPx()),
                strokeWidth = 4.dp.toPx()
            )

            // Left Tread
            val leftColor = if (lcdMode) {
                if (trackLeft != 0) LcdInk else LcdInkSecondary
            } else {
                if (trackLeft > 0) NeonGreen else if (trackLeft < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
            }
            drawRoundRect(
                color = leftColor,
                topLeft = Offset(centerX - 38.dp.toPx(), centerY - 40.dp.toPx()),
                size = Size(14.dp.toPx(), 80.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            val segLineColor = if (lcdMode) LcdScreenBg else Color.Black.copy(alpha = 0.6f)
            // Left tread inner segments
            for (i in 0..5) {
                val ySeg = centerY - 35.dp.toPx() + (i * 14.dp.toPx())
                drawLine(
                    color = segLineColor,
                    start = Offset(centerX - 37.dp.toPx(), ySeg),
                    end = Offset(centerX - 25.dp.toPx(), ySeg),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Right Tread
            val rightColor = if (lcdMode) {
                if (trackRight != 0) LcdInk else LcdInkSecondary
            } else {
                if (trackRight > 0) NeonGreen else if (trackRight < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
            }
            drawRoundRect(
                color = rightColor,
                topLeft = Offset(centerX + 24.dp.toPx(), centerY - 40.dp.toPx()),
                size = Size(14.dp.toPx(), 80.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            // Right tread inner segments
            for (i in 0..5) {
                val ySeg = centerY - 35.dp.toPx() + (i * 14.dp.toPx())
                drawLine(
                    color = segLineColor,
                    start = Offset(centerX + 25.dp.toPx(), ySeg),
                    end = Offset(centerX + 37.dp.toPx(), ySeg),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun ServoCarChassisGraphic(
    throttle: Int,
    servoAngle: Int,
    modifier: Modifier = Modifier
) {
    val lcdMode = isCarbonLcdTheme
    // Map 0..180 servo degrees to steering wheel visual deflection (-35° to +35°)
    val steerAngle = ((servoAngle - 90) * 0.40f).coerceIn(-35f, 35f)
    val animatedSteerAngle by animateFloatAsState(targetValue = steerAngle, animationSpec = tween(60), label = "steer")

    Box(
        modifier = modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(width = 120.dp, height = 110.dp),
            contentAlignment = Alignment.Center
        ) {
            // Static Body Canvas (Hull, Rear Wheels, Axles)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                // Chassis Body
                drawRoundRect(
                    color = if (lcdMode) LcdGhostSegment else CyberSurfaceVariant.copy(alpha = 0.8f),
                    topLeft = Offset(centerX - 20.dp.toPx(), centerY - 36.dp.toPx()),
                    size = Size(40.dp.toPx(), 72.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawRoundRect(
                    color = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.6f),
                    topLeft = Offset(centerX - 20.dp.toPx(), centerY - 36.dp.toPx()),
                    size = Size(40.dp.toPx(), 72.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = if (lcdMode) 2.dp.toPx() else 1.5.dp.toPx())
                )

                val axleColor = if (lcdMode) LcdInk else CyberPrimary.copy(alpha = 0.6f)
                // Front Axle Bar
                drawLine(
                    color = axleColor,
                    start = Offset(centerX - 28.dp.toPx(), centerY - 24.dp.toPx()),
                    end = Offset(centerX + 28.dp.toPx(), centerY - 24.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Rear Axle Bar
                drawLine(
                    color = axleColor,
                    start = Offset(centerX - 28.dp.toPx(), centerY + 24.dp.toPx()),
                    end = Offset(centerX + 28.dp.toPx(), centerY + 24.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Rear Wheels (Driven by throttle)
                val rearColor = if (lcdMode) {
                    if (throttle != 0) LcdInk else LcdInkSecondary
                } else {
                    if (throttle > 0) NeonGreen else if (throttle < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
                }
                // Rear Left Wheel
                drawRoundRect(
                    color = rearColor,
                    topLeft = Offset(centerX - 36.dp.toPx(), centerY + 14.dp.toPx()),
                    size = Size(10.dp.toPx(), 22.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                // Rear Right Wheel
                drawRoundRect(
                    color = rearColor,
                    topLeft = Offset(centerX + 26.dp.toPx(), centerY + 14.dp.toPx()),
                    size = Size(10.dp.toPx(), 22.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Cockpit windshield accent
                drawRoundRect(
                    color = if (lcdMode) LcdInk.copy(alpha = 0.7f) else CyberPrimary.copy(alpha = 0.3f),
                    topLeft = Offset(centerX - 12.dp.toPx(), centerY - 14.dp.toPx()),
                    size = Size(24.dp.toPx(), 22.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Dynamic Rotating Front Wheels using Compose Layout
            // Front Left Wheel
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 20.dp)
                    .graphicsLayer {
                        rotationZ = animatedSteerAngle
                    }
                    .size(width = 10.dp, height = 22.dp)
                    .background(if (lcdMode) LcdInk else CyberPrimary, RoundedCornerShape(3.dp))
                    .border(1.dp, if (lcdMode) LcdScreenBg else Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            )

            // Front Right Wheel
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 24.dp, top = 20.dp)
                    .graphicsLayer {
                        rotationZ = animatedSteerAngle
                    }
                    .size(width = 10.dp, height = 22.dp)
                    .background(if (lcdMode) LcdInk else CyberPrimary, RoundedCornerShape(3.dp))
                    .border(1.dp, if (lcdMode) LcdScreenBg else Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            )
        }
    }
}
