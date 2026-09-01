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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.androidbljoy.theme.CyberSecondary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.NeonAmber
import com.example.androidbljoy.theme.NeonGreen
import com.example.androidbljoy.theme.NeonRed
import com.example.androidbljoy.theme.OffWhite
import kotlin.math.abs

@Composable
fun VehicleVisualizer(
    vehicleType: DrivingMode,
    motorA: Int,
    motorB: Int,
    servoAngle: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CyberPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(12.dp)
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
                modifier = Modifier.fillMaxWidth(),
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
                    color = CyberPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "TELEMETRÍA PWM",
                    color = MutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
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
                    DrivingMode.DUAL_DC, DrivingMode.ARCADE -> {
                        // Left Motor PWM Bar
                        PwmVerticalBar(
                            label = "MTR A (Izq)",
                            value = motorA,
                            modifier = Modifier.width(36.dp).fillMaxHeight()
                        )

                        // Central Vehicle Chassis Graphic
                        DualMotorChassisGraphic(
                            motorA = motorA,
                            motorB = motorB,
                            isArcade = (vehicleType == DrivingMode.ARCADE),
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
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A: ${if (motorA > 0) "+$motorA" else "$motorA"}",
                    color = if (motorA > 0) NeonGreen else if (motorA < 0) NeonRed else MutedText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "B: ${if (motorB > 0) "+$motorB" else "$motorB"}",
                    color = if (motorB > 0) NeonGreen else if (motorB < 0) NeonRed else MutedText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                if (vehicleType == DrivingMode.SERVO_CAR) {
                    Text(
                        text = "SERVO: $servoAngle°",
                        color = CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
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
    val clamped = value.coerceIn(-maxValue, maxValue)
    val ratio = abs(clamped) / maxValue.toFloat()
    val isForward = clamped > 0
    val isReverse = clamped < 0

    val barColor = when {
        isForward -> NeonGreen
        isReverse -> NeonAmber
        else -> CyberSurfaceVariant
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = MutedText,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(14.dp)
                .weight(1f)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .border(1.dp, CyberSurfaceVariant, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midY = size.height / 2f

                // Center zero line
                drawLine(
                    color = CyberPrimary.copy(alpha = 0.7f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 1.5f
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
            color = if (clamped != 0) barColor else MutedText,
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
            color = MutedText,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(14.dp)
                .weight(1f)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .border(1.dp, CyberSurfaceVariant, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midY = size.height / 2f

                // Center zero line (90°)
                drawLine(
                    color = CyberPrimary,
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 2f
                )

                if (offsetFromCenter != 0) {
                    val barHeight = midY * ratio
                    val top = if (offsetFromCenter > 0) midY - barHeight else midY
                    drawRoundRect(
                        color = CyberPrimary,
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
            color = CyberPrimary,
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
    Box(
        modifier = modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 120.dp, height = 110.dp)) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f

            // Central Rover Chassis Body
            drawRoundRect(
                color = CyberSurfaceVariant.copy(alpha = 0.7f),
                topLeft = Offset(centerX - 24.dp.toPx(), centerY - 38.dp.toPx()),
                size = Size(48.dp.toPx(), 76.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = CyberPrimary.copy(alpha = 0.6f),
                topLeft = Offset(centerX - 24.dp.toPx(), centerY - 38.dp.toPx()),
                size = Size(48.dp.toPx(), 76.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Front direction indicator arrow (Forward ▲)
            val arrowPath = Path().apply {
                moveTo(centerX, centerY - 28.dp.toPx())
                lineTo(centerX - 8.dp.toPx(), centerY - 16.dp.toPx())
                lineTo(centerX + 8.dp.toPx(), centerY - 16.dp.toPx())
                close()
            }
            drawPath(arrowPath, color = CyberPrimary)

            // Left Wheels (Motor A)
            val leftColor = if (motorA > 0) NeonGreen else if (motorA < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
            // Front Left
            drawRoundRect(
                color = leftColor,
                topLeft = Offset(centerX - 38.dp.toPx(), centerY - 34.dp.toPx()),
                size = Size(10.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            // Rear Left
            drawRoundRect(
                color = leftColor,
                topLeft = Offset(centerX - 38.dp.toPx(), centerY + 12.dp.toPx()),
                size = Size(10.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Right Wheels (Motor B)
            val rightColor = if (motorB > 0) NeonGreen else if (motorB < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
            // Front Right
            drawRoundRect(
                color = rightColor,
                topLeft = Offset(centerX + 28.dp.toPx(), centerY - 34.dp.toPx()),
                size = Size(10.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            // Rear Right
            drawRoundRect(
                color = rightColor,
                topLeft = Offset(centerX + 28.dp.toPx(), centerY + 12.dp.toPx()),
                size = Size(10.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    }
}

@Composable
fun TankChassisGraphic(
    trackLeft: Int,
    trackRight: Int,
    modifier: Modifier = Modifier
) {
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
                color = CyberSurfaceVariant,
                topLeft = Offset(centerX - 20.dp.toPx(), centerY - 34.dp.toPx()),
                size = Size(40.dp.toPx(), 68.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            drawRoundRect(
                color = CyberPrimary.copy(alpha = 0.5f),
                topLeft = Offset(centerX - 20.dp.toPx(), centerY - 34.dp.toPx()),
                size = Size(40.dp.toPx(), 68.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Turret circle
            drawCircle(
                color = CyberPrimary.copy(alpha = 0.8f),
                radius = 12.dp.toPx(),
                center = Offset(centerX, centerY)
            )

            // Cannon Barrel pointing up
            drawLine(
                color = CyberPrimary,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - 32.dp.toPx()),
                strokeWidth = 4.dp.toPx()
            )

            // Left Tread
            val leftColor = if (trackLeft > 0) NeonGreen else if (trackLeft < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
            drawRoundRect(
                color = leftColor,
                topLeft = Offset(centerX - 38.dp.toPx(), centerY - 40.dp.toPx()),
                size = Size(14.dp.toPx(), 80.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            // Left tread inner segments
            for (i in 0..5) {
                val ySeg = centerY - 35.dp.toPx() + (i * 14.dp.toPx())
                drawLine(
                    color = Color.Black.copy(alpha = 0.6f),
                    start = Offset(centerX - 37.dp.toPx(), ySeg),
                    end = Offset(centerX - 25.dp.toPx(), ySeg),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Right Tread
            val rightColor = if (trackRight > 0) NeonGreen else if (trackRight < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
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
                    color = Color.Black.copy(alpha = 0.6f),
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
                    color = CyberSurfaceVariant.copy(alpha = 0.8f),
                    topLeft = Offset(centerX - 20.dp.toPx(), centerY - 36.dp.toPx()),
                    size = Size(40.dp.toPx(), 72.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawRoundRect(
                    color = CyberPrimary.copy(alpha = 0.6f),
                    topLeft = Offset(centerX - 20.dp.toPx(), centerY - 36.dp.toPx()),
                    size = Size(40.dp.toPx(), 72.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Front Axle Bar
                drawLine(
                    color = CyberPrimary.copy(alpha = 0.6f),
                    start = Offset(centerX - 28.dp.toPx(), centerY - 24.dp.toPx()),
                    end = Offset(centerX + 28.dp.toPx(), centerY - 24.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Rear Axle Bar
                drawLine(
                    color = CyberPrimary.copy(alpha = 0.6f),
                    start = Offset(centerX - 28.dp.toPx(), centerY + 24.dp.toPx()),
                    end = Offset(centerX + 28.dp.toPx(), centerY + 24.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Rear Wheels (Driven by throttle)
                val rearColor = if (throttle > 0) NeonGreen else if (throttle < 0) NeonAmber else OffWhite.copy(alpha = 0.4f)
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
                    color = CyberPrimary.copy(alpha = 0.3f),
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
                    .background(CyberPrimary, RoundedCornerShape(3.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
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
                    .background(CyberPrimary, RoundedCornerShape(3.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            )
        }
    }
}
