package com.example.androidbljoy.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.ui.components.CustomSwitch
import com.example.androidbljoy.ui.components.ExpoGraph
import com.example.androidbljoy.ui.main.MainScreenViewModel

@Composable
fun InputsTab(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val tractionVal by viewModel.tractionValue.collectAsStateWithLifecycle()
    val steeringVal by viewModel.steeringValue.collectAsStateWithLifecycle()

    val inputs = activeModel.inputs

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section: Curvas Exponenciales (Expo) con Gráfico Interactivo
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
                        text = "Curvas Exponenciales (Inputs Expo)",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Suaviza la respuesta alrededor del centro del stick sin reducir la velocidad máxima.",
                        color = MutedText,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tracción Expo
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Expo Tracción", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${inputs.tractionExpo}%", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = inputs.tractionExpo.toFloat(),
                                        onValueChange = { viewModel.updateInputs { inp -> inp.copy(tractionExpo = it.toInt()) } },
                                        valueRange = 0f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = CyberPrimary,
                                            activeTrackColor = CyberPrimary,
                                            inactiveTrackColor = CyberSurfaceVariant
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                ExpoGraph(
                                    expoPercent = inputs.tractionExpo,
                                    currentInput = tractionVal,
                                    modifier = Modifier.width(90.dp).height(65.dp)
                                )
                            }
                        }

                        // Dirección Expo
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Expo Dirección", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${inputs.steeringExpo}%", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = inputs.steeringExpo.toFloat(),
                                        onValueChange = { viewModel.updateInputs { inp -> inp.copy(steeringExpo = it.toInt()) } },
                                        valueRange = 0f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = CyberPrimary,
                                            activeTrackColor = CyberPrimary,
                                            inactiveTrackColor = CyberSurfaceVariant
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                ExpoGraph(
                                    expoPercent = inputs.steeringExpo,
                                    currentInput = steeringVal,
                                    modifier = Modifier.width(90.dp).height(65.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Zonas Muertas de Stick (Deadzone)
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
                        text = "Zona Muerta del Stick (Deadzone)",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Evita derivas involuntarias causadas por el temblor de los dedos o descalibración del centro.",
                        color = MutedText,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Deadzone Tracción
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deadzone Tracción", color = OffWhite, fontSize = 11.sp)
                                Text("${(inputs.tractionDeadzone * 100).toInt()}%", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = inputs.tractionDeadzone,
                                onValueChange = { viewModel.updateInputs { inp -> inp.copy(tractionDeadzone = it) } },
                                valueRange = 0f..0.25f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimary,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // Deadzone Dirección
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deadzone Dirección", color = OffWhite, fontSize = 11.sp)
                                Text("${(inputs.steeringDeadzone * 100).toInt()}%", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = inputs.steeringDeadzone,
                                onValueChange = { viewModel.updateInputs { inp -> inp.copy(steeringDeadzone = it) } },
                                valueRange = 0f..0.25f,
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

        // Section: Configuración de los Joysticks
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
                        text = "Disposición de Mandos en Pantalla",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Joystick Unificado (Una sola mano)", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Controla aceleración y dirección en un solo joystick 2D", color = MutedText, fontSize = 9.sp)
                        }
                        CustomSwitch(
                            checked = inputs.unifiedJoystick,
                            onCheckedChange = { viewModel.updateInputs { inp -> inp.copy(unifiedJoystick = it) } }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (inputs.unifiedJoystick) "Ubicar Joystick en la Derecha" else "Intercambiar Joysticks (Modo Zurdos)", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Invierte las posiciones de los controles en pantalla", color = MutedText, fontSize = 9.sp)
                        }
                        CustomSwitch(
                            checked = inputs.swapJoysticks,
                            onCheckedChange = { viewModel.updateInputs { inp -> inp.copy(swapJoysticks = it) } }
                        )
                    }
                }
            }
        }
    }
}
