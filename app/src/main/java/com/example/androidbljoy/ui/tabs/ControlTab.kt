package com.example.androidbljoy.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSecondary
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.LcdBezelOuter
import com.example.androidbljoy.theme.LcdInk
import com.example.androidbljoy.theme.LcdInkSecondary
import com.example.androidbljoy.theme.LcdScreenBg
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.NeonRed
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.theme.isCarbonLcdTheme
import com.example.androidbljoy.ui.components.Joystick
import com.example.androidbljoy.ui.components.VehicleVisualizer
import com.example.androidbljoy.ui.main.MainScreenViewModel

@Composable
fun ControlTab(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val tractionVal by viewModel.tractionValue.collectAsStateWithLifecycle()
    val steeringVal by viewModel.steeringValue.collectAsStateWithLifecycle()

    val motorA by viewModel.outputMotorA.collectAsStateWithLifecycle()
    val motorB by viewModel.outputMotorB.collectAsStateWithLifecycle()
    val servoAngle by viewModel.outputServo.collectAsStateWithLifecycle()

    val lastSentMessage by viewModel.lastSentMessage.collectAsStateWithLifecycle()
    val tractionTrimLocked by viewModel.tractionTrimLocked.collectAsStateWithLifecycle()
    val steeringTrimLocked by viewModel.steeringTrimLocked.collectAsStateWithLifecycle()

    val unifiedJoystick = activeModel.inputs.unifiedJoystick
    val swapJoysticks = activeModel.inputs.swapJoysticks
    val drivingMode = activeModel.vehicleType
    val tractionTrim = activeModel.inputs.tractionTrim
    val steeringTrim = activeModel.inputs.steeringTrim

    val leftColumnContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        if (unifiedJoystick && drivingMode != DrivingMode.TANK) {
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(2.4f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Joystick(
                        name = "Control Múltiple",
                        isVerticalOnly = false,
                        isHorizontalOnly = false,
                        externalX = steeringVal,
                        externalY = tractionVal,
                        onValueChanged = { x, y ->
                            viewModel.updateSteering(x)
                            viewModel.updateTraction(y)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.weight(0.6f).fillMaxWidth())
            }
        } else {
            val tractionIsVertical = activeModel.inputs.tractionAxisVertical
            val leftJoyLabel = if (drivingMode == DrivingMode.TANK) {
                if (tractionIsVertical) "Oruga Izq (Y)" else "Oruga Izq (X)"
            } else {
                if (tractionIsVertical) "Tracción (Y)" else "Tracción (X)"
            }

            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(2.4f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Joystick(
                        name = leftJoyLabel,
                        isVerticalOnly = tractionIsVertical,
                        isHorizontalOnly = !tractionIsVertical,
                        externalX = if (!tractionIsVertical) tractionVal else null,
                        externalY = if (tractionIsVertical) tractionVal else null,
                        onValueChanged = { x, y ->
                            if (tractionIsVertical) viewModel.updateTraction(y) else viewModel.updateTraction(x)
                        }
                    )

                    // Traction Trim Slider & Lock
                    if (tractionIsVertical) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleTractionTrimLock() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Trim",
                                    tint = if (tractionTrimLocked) NeonRed else MutedText.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${if (tractionTrim > 0) "+" else ""}$tractionTrim",
                                color = OffWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.height(180.dp).width(30.dp)
                            ) {
                                Slider(
                                    value = tractionTrim.toFloat(),
                                    onValueChange = { viewModel.setTractionTrim(it.toInt()) },
                                    valueRange = -50f..50f,
                                    enabled = !tractionTrimLocked,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (!tractionTrimLocked) CyberPrimary else MutedText,
                                        activeTrackColor = if (!tractionTrimLocked) CyberPrimary else MutedText.copy(alpha = 0.5f),
                                        inactiveTrackColor = CyberSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .requiredWidth(180.dp)
                                        .graphicsLayer {
                                            rotationZ = -90f
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                                        }
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleTractionTrimLock() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Trim",
                                    tint = if (tractionTrimLocked) NeonRed else MutedText.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${if (tractionTrim > 0) "+" else ""}$tractionTrim",
                                color = OffWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(26.dp),
                                textAlign = TextAlign.Center
                            )
                            Slider(
                                value = tractionTrim.toFloat(),
                                onValueChange = { viewModel.setTractionTrim(it.toInt()) },
                                valueRange = -50f..50f,
                                enabled = !tractionTrimLocked,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (!tractionTrimLocked) CyberPrimary else MutedText,
                                    activeTrackColor = if (!tractionTrimLocked) CyberPrimary else MutedText.copy(alpha = 0.5f),
                                    inactiveTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.width(160.dp).height(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                        .padding(horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PressHoldButton(
                        text = if (tractionIsVertical) "▲ Avanzar" else "Avanzar ▶",
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        onPressed = { viewModel.updateTraction(1.0f) },
                        onReleased = { viewModel.updateTraction(0f) }
                    )
                    PressHoldButton(
                        text = if (tractionIsVertical) "▼ Retroceder" else "◀ Retroceder",
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        onPressed = { viewModel.updateTraction(-1.0f) },
                        onReleased = { viewModel.updateTraction(0f) }
                    )
                }
            }
        }
    }

    val rightColumnContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        if (unifiedJoystick && drivingMode != DrivingMode.TANK) {
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(CyberSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Modo Joystick Unificado Activo.\nControla todo desde el joystick principal.",
                        color = MutedText,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val steeringIsHorizontal = if (drivingMode == DrivingMode.TANK) false else activeModel.inputs.steeringAxisHorizontal
            val rightJoyLabel = if (drivingMode == DrivingMode.TANK) {
                "Oruga Der (Y)"
            } else {
                if (steeringIsHorizontal) "Dirección (X)" else "Dirección (Y)"
            }

            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(2.4f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Joystick(
                        name = rightJoyLabel,
                        isVerticalOnly = !steeringIsHorizontal,
                        isHorizontalOnly = steeringIsHorizontal,
                        isDigital = false,
                        externalX = if (steeringIsHorizontal) steeringVal else null,
                        externalY = if (!steeringIsHorizontal) steeringVal else null,
                        onValueChanged = { x, y ->
                            if (steeringIsHorizontal) {
                                viewModel.updateSteering(x)
                            } else {
                                viewModel.updateSteering(y)
                            }
                        }
                    )

                    // Steering Trim Slider (Horizontal or Vertical depending on chosen axis)
                    if (steeringIsHorizontal) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleSteeringTrimLock() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Steering Trim",
                                    tint = if (steeringTrimLocked) NeonRed else MutedText.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${if (steeringTrim > 0) "+" else ""}$steeringTrim",
                                color = OffWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(26.dp),
                                textAlign = TextAlign.Center
                            )
                            Slider(
                                value = steeringTrim.toFloat(),
                                onValueChange = { viewModel.setSteeringTrim(it.toInt()) },
                                valueRange = -50f..50f,
                                enabled = !steeringTrimLocked,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (!steeringTrimLocked) CyberPrimary else MutedText,
                                    activeTrackColor = if (!steeringTrimLocked) CyberPrimary else MutedText.copy(alpha = 0.5f),
                                    inactiveTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.width(160.dp).height(20.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleSteeringTrimLock() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Steering Trim",
                                    tint = if (steeringTrimLocked) NeonRed else MutedText.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${if (steeringTrim > 0) "+" else ""}$steeringTrim",
                                color = OffWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.height(180.dp).width(30.dp)
                            ) {
                                Slider(
                                    value = steeringTrim.toFloat(),
                                    onValueChange = { viewModel.setSteeringTrim(it.toInt()) },
                                    valueRange = -50f..50f,
                                    enabled = !steeringTrimLocked,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (!steeringTrimLocked) CyberPrimary else MutedText,
                                        activeTrackColor = if (!steeringTrimLocked) CyberPrimary else MutedText.copy(alpha = 0.5f),
                                        inactiveTrackColor = CyberSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .requiredWidth(180.dp)
                                        .graphicsLayer {
                                            rotationZ = -90f
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (drivingMode == DrivingMode.TANK) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                            .padding(horizontal = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PressHoldButton(
                            text = "▲ Avanzar R",
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            onPressed = { viewModel.updateSteering(1.0f) },
                            onReleased = { viewModel.updateSteering(0f) }
                        )
                        PressHoldButton(
                            text = "▼ Retroceder R",
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            onPressed = { viewModel.updateSteering(-1.0f) },
                            onReleased = { viewModel.updateSteering(0f) }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PressHoldButton(
                            text = "◀ Izquierda",
                            modifier = Modifier.fillMaxHeight().weight(1f),
                            onPressed = { viewModel.updateSteering(-1.0f) },
                            onReleased = { viewModel.updateSteering(0f) }
                        )
                        PressHoldButton(
                            text = "Derecha ▶",
                            modifier = Modifier.fillMaxHeight().weight(1f),
                            onPressed = { viewModel.updateSteering(1.0f) },
                            onReleased = { viewModel.updateSteering(0f) }
                        )
                    }
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column (Traction or swapped)
        if (swapJoysticks) {
            rightColumnContent()
        } else {
            leftColumnContent()
        }

        // CENTER COLUMN: Real-Time Vehicle Visualizer & Telemetry
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Interactive Vehicle Telemetry Visualizer
            VehicleVisualizer(
                vehicleType = drivingMode,
                motorA = motorA,
                motorB = motorB,
                servoAngle = servoAngle,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // GATT Stream Output Bar
            val lcdMode = isCarbonLcdTheme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (lcdMode) LcdScreenBg else Color.Black.copy(alpha = 0.8f),
                        RoundedCornerShape(if (lcdMode) 6.dp else 8.dp)
                    )
                    .then(
                        if (lcdMode) {
                            Modifier.border(2.5.dp, LcdBezelOuter, RoundedCornerShape(6.dp))
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GATT: $lastSentMessage",
                        color = if (lcdMode) LcdInk else CyberSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CH1:${(tractionVal * 100).toInt()}% CH2:${(steeringVal * 100).toInt()}%",
                        color = if (lcdMode) LcdInkSecondary else OffWhite.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = if (lcdMode) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Right Column (Steering or swapped)
        if (swapJoysticks) {
            leftColumnContent()
        } else {
            rightColumnContent()
        }
    }
}

@Composable
fun PressHoldButton(
    text: String,
    modifier: Modifier = Modifier,
    activeColor: Color = CyberPrimary,
    onPressed: () -> Unit,
    onReleased: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val carbonMode = isCarbonLcdTheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (carbonMode) 6.dp else 8.dp))
            .background(
                when {
                    isPressed -> activeColor.copy(alpha = 0.35f)
                    carbonMode -> Color(0xFF101215)
                    else -> CyberSurfaceVariant.copy(alpha = 0.85f)
                }
            )
            .border(
                1.5.dp,
                when {
                    isPressed -> activeColor
                    carbonMode -> Color(0xFF3E4550)
                    else -> CyberSurfaceVariant
                },
                RoundedCornerShape(if (carbonMode) 6.dp else 8.dp)
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    onPressed()
                    val upOrCancel = waitForUpOrCancellation()
                    upOrCancel?.consume()
                    isPressed = false
                    onReleased()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPressed) activeColor else OffWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
