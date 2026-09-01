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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                        DrivingMode.DUAL_DC, DrivingMode.ARCADE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Canal Tracción / Motor A", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertTraction,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertTraction = it) } }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Canal Dirección / Motor B", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertSteering,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertSteering = it) } }
                                )
                            }
                        }

                        DrivingMode.TANK -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Oruga Izquierda", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertLeftTrack,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertLeftTrack = it) } }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invertir Oruga Derecha", color = OffWhite, fontSize = 11.sp)
                                CustomSwitch(
                                    checked = outputs.invertRightTrack,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertRightTrack = it) } }
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
                                    checked = outputs.invertTraction,
                                    onCheckedChange = { viewModel.updateOutputs { o -> o.copy(invertTraction = it) } }
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "EPA (End Point Adjustment / Límites Máximos)",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Limita la potencia o ángulo máximo para proteger la mecánica y evitar forzar los actuadores.",
                        color = MutedText,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // EPA Tracción
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("EPA Tracción (PWM Máx)", color = OffWhite, fontSize = 11.sp)
                                Text("${outputs.tractionLimit}", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = outputs.tractionLimit.toFloat(),
                                onValueChange = { viewModel.updateOutputs { o -> o.copy(tractionLimit = it.toInt()) } },
                                valueRange = 50f..255f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimary,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // EPA Dirección o Servo
                        if (mode == DrivingMode.SERVO_CAR) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("EPA Servo Dirección", color = OffWhite, fontSize = 11.sp)
                                    Text("${outputs.epaSteering}%", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = outputs.epaSteering.toFloat(),
                                    onValueChange = { viewModel.updateOutputs { o -> o.copy(epaSteering = it.toInt()) } },
                                    valueRange = 10f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = CyberPrimary,
                                        activeTrackColor = CyberPrimary,
                                        inactiveTrackColor = CyberSurfaceVariant
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        } else {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("EPA Dirección (PWM Máx)", color = OffWhite, fontSize = 11.sp)
                                    Text("${outputs.steeringLimit}", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = outputs.steeringLimit.toFloat(),
                                    onValueChange = { viewModel.updateOutputs { o -> o.copy(steeringLimit = it.toInt()) } },
                                    valueRange = 50f..255f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = CyberPrimary,
                                        activeTrackColor = CyberPrimary,
                                        inactiveTrackColor = CyberSurfaceVariant
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }

                    // Subtrim for Servo Car
                    if (mode == DrivingMode.SERVO_CAR) {
                        Spacer(modifier = Modifier.height(4.dp))
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
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimary,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.height(24.dp)
                            )
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
