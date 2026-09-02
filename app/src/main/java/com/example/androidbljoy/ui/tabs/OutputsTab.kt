package com.example.androidbljoy.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.ui.components.CustomSwitch
import com.example.androidbljoy.ui.main.MainScreenViewModel

@Composable
fun OutputsTab(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val outputs = activeModel.outputs
    val mode = activeModel.vehicleType

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section: Inversión de Sentido (Reverse por Canal)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Inversión de Sentido Físico (Reverse)",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Invierte la rotación del motor o el giro del servo si los cables o varillas mecánicas están al revés.",
                        color = MutedText,
                        fontSize = 10.sp
                    )

                    when (mode) {
                        DrivingMode.ARCADE, DrivingMode.TANK -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Motor A (Rueda / Oruga Izq)", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertMotorA,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertMotorA = it) } }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Motor B (Rueda / Oruga Der)", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertMotorB,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertMotorB = it) } }
                                )
                            }
                        }

                        DrivingMode.DUAL_DC -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Motor A (Tracción Trasera)", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertMotorA,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertMotorA = it) } }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Motor B (Dirección DC Delantera)", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertMotorB,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertMotorB = it) } }
                                )
                            }
                        }

                        DrivingMode.SERVO_CAR -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Tracción Motor", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertMotorA,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertMotorA = it) } }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Giro del Servo de Dirección", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertServo,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertServo = it) } }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: EPA (End Point Adjustment / Límites de Recorrido)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "EPA (End Point Adjustment / Límites de Recorrido)",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configura los límites máximos independientes para proteger la mecánica y ajustar el recorrido superior e inferior.",
                        color = MutedText,
                        fontSize = 10.sp
                    )

                    when (mode) {
                        DrivingMode.ARCADE, DrivingMode.TANK -> {
                            MotorEpaRangeControl(
                                title = "BANDA EPA MOTOR A (IZQUIERDA)",
                                subtitle = "Rango de salida física para la rueda u oruga izquierda",
                                minVal = outputs.minEpaMotorA,
                                maxVal = outputs.maxEpaMotorA,
                                onRangeChange = { min, max ->
                                    viewModel.updateOutputs { o -> o.copy(minEpaMotorA = min, maxEpaMotorA = max) }
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            MotorEpaRangeControl(
                                title = "BANDA EPA MOTOR B (DERECHA)",
                                subtitle = "Rango de salida física para la rueda u oruga derecha",
                                minVal = outputs.minEpaMotorB,
                                maxVal = outputs.maxEpaMotorB,
                                onRangeChange = { min, max ->
                                    viewModel.updateOutputs { o -> o.copy(minEpaMotorB = min, maxEpaMotorB = max) }
                                }
                            )
                        }

                        DrivingMode.DUAL_DC -> {
                            MotorEpaRangeControl(
                                title = "BANDA EPA MOTOR A (TRACCIÓN TRASERA)",
                                subtitle = "Rango de potencia Reversa / Avance",
                                minVal = outputs.minEpaMotorA,
                                maxVal = outputs.maxEpaMotorA,
                                onRangeChange = { min, max ->
                                    viewModel.updateOutputs { o -> o.copy(minEpaMotorA = min, maxEpaMotorA = max) }
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            MotorEpaRangeControl(
                                title = "BANDA EPA MOTOR B (DIRECCIÓN DC DELANTERA)",
                                subtitle = "Rango de potencia Giro Izq / Giro Der",
                                minVal = outputs.minEpaMotorB,
                                maxVal = outputs.maxEpaMotorB,
                                onRangeChange = { min, max ->
                                    viewModel.updateOutputs { o -> o.copy(minEpaMotorB = min, maxEpaMotorB = max) }
                                }
                            )
                        }

                        DrivingMode.SERVO_CAR -> {
                            MotorEpaRangeControl(
                                title = "BANDA EPA MOTOR DE TRACCIÓN",
                                subtitle = "Rango de potencia Reversa / Avance",
                                minVal = outputs.minEpaMotorA,
                                maxVal = outputs.maxEpaMotorA,
                                onRangeChange = { min, max ->
                                    viewModel.updateOutputs { o -> o.copy(minEpaMotorA = min, maxEpaMotorA = max) }
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "LÍMITES DE DIRECCIÓN (SERVO)",
                                color = OffWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("End Point Izquierdo (Giro Izq)", color = OffWhite, fontSize = 11.sp)
                                        Text("${outputs.epaServoLeft}%", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = outputs.epaServoLeft.toFloat(),
                                        onValueChange = { viewModel.updateOutputs { o -> o.copy(epaServoLeft = it.toInt()) } },
                                        valueRange = 10f..100f,
                                        colors = SliderDefaults.colors(thumbColor = CyberPrimary, activeTrackColor = CyberPrimary, inactiveTrackColor = CyberSurfaceVariant),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("End Point Derecho (Giro Der)", color = OffWhite, fontSize = 11.sp)
                                        Text("${outputs.epaServoRight}%", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = outputs.epaServoRight.toFloat(),
                                        onValueChange = { viewModel.updateOutputs { o -> o.copy(epaServoRight = it.toInt()) } },
                                        valueRange = 10f..100f,
                                        colors = SliderDefaults.colors(thumbColor = CyberPrimary, activeTrackColor = CyberPrimary, inactiveTrackColor = CyberSurfaceVariant),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtrim Servo (Centro Mecánico)", color = OffWhite, fontSize = 11.sp)
                                    Text("${if (outputs.trimSteering >= 0) "+" else ""}${outputs.trimSteering}°", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = outputs.trimSteering.toFloat(),
                                    onValueChange = { viewModel.updateOutputs { o -> o.copy(trimSteering = it.toInt()) } },
                                    valueRange = -45f..45f,
                                    colors = SliderDefaults.colors(thumbColor = CyberPrimary, activeTrackColor = CyberPrimary, inactiveTrackColor = CyberSurfaceVariant),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Deadband de Motores & Tipo de Hardware (DC vs ESC)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Umbral de Motores y Modo de Hardware",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "El deadband compensa la fricción inicial para que el motor empiece a rodar de inmediato.",
                        color = MutedText,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deadband Motor A", color = OffWhite, fontSize = 11.sp)
                                Text("${outputs.deadbandA}", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = outputs.deadbandA.toFloat(),
                                onValueChange = { viewModel.updateOutputs { o -> o.copy(deadbandA = it.toInt()) } },
                                valueRange = 0f..127f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimary,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deadband Motor B", color = OffWhite, fontSize = 11.sp)
                                Text("${outputs.deadbandB}", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = outputs.deadbandB.toFloat(),
                                onValueChange = { viewModel.updateOutputs { o -> o.copy(deadbandB = it.toInt()) } },
                                valueRange = 0f..127f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimary,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Hardware Driver Selector (DC vs ESC)
                    Text("Protocolo de Salida / Hardware Driver", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isDc = outputs.tractionHardwareMode == 0
                        Button(
                            onClick = { viewModel.updateOutputs { o -> o.copy(tractionHardwareMode = 0) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDc) CyberPrimary else CyberSurfaceVariant,
                                contentColor = if (isDc) Color.Black else OffWhite
                            ),
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Driver DC (Puente H)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.updateOutputs { o -> o.copy(tractionHardwareMode = 1) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDc) CyberPrimary else CyberSurfaceVariant,
                                contentColor = if (!isDc) Color.Black else OffWhite
                            ),
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("ESC (Speed Controller)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MotorEpaRangeControl(
    title: String,
    subtitle: String,
    minVal: Int,
    maxVal: Int,
    onRangeChange: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MutedText, fontSize = 9.sp)
            }

            val badgeText = when {
                minVal < 0 && maxVal > 0 -> "Banda: [$minVal ... +$maxVal]"
                minVal >= 0 && maxVal > 0 -> "Solo Avance [+$minVal ... +$maxVal]"
                minVal < 0 && maxVal <= 0 -> "Solo Reversa [$minVal ... $maxVal]"
                else -> "Bloqueado [0]"
            }
            Text(
                text = badgeText,
                color = CyberPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        RangeSlider(
            value = minVal.toFloat()..maxVal.toFloat(),
            onValueChange = { range ->
                val newMin = range.start.toInt().coerceIn(-255, 255)
                val newMax = range.endInclusive.toInt().coerceIn(-255, 255)
                onRangeChange(newMin, newMax)
            },
            valueRange = -255f..255f,
            colors = SliderDefaults.colors(
                thumbColor = CyberPrimary,
                activeTrackColor = CyberPrimary,
                inactiveTrackColor = CyberSurface
            ),
            modifier = Modifier.height(28.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Reversa Máx: $minVal", color = MutedText, fontSize = 9.sp)
            Text("Centro: 0", color = MutedText, fontSize = 8.sp)
            Text("Avance Máx: +$maxVal", color = MutedText, fontSize = 9.sp)
        }
    }
}
