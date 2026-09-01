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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun MixerTab(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val mixer = activeModel.mixer
    val currentMode = activeModel.vehicleType

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector Cards
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
                        text = "Tipo de Vehículo y Algoritmo de Mezcla",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "El mezclador traduce los movimientos de los sticks en los canales correctos de tu vehículo.",
                        color = MutedText,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DrivingMode.values().forEach { mode ->
                            val isSelected = (currentMode == mode)
                            val title = when (mode) {
                                DrivingMode.DUAL_DC -> "Doble Motor"
                                DrivingMode.TANK -> "Tanque Oruga"
                                DrivingMode.SERVO_CAR -> "Coche Servo"
                                DrivingMode.ARCADE -> "Arcade"
                            }
                            val desc = when (mode) {
                                DrivingMode.DUAL_DC -> "Ejes indep."
                                DrivingMode.TANK -> "Oruga L / R"
                                DrivingMode.SERVO_CAR -> "Tracción + Dir"
                                DrivingMode.ARCADE -> "Mezcla L=Y+X"
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setVehicleType(mode) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CyberPrimary.copy(alpha = 0.2f) else CyberSurfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) CyberPrimary else CyberSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) CyberPrimary else OffWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        color = MutedText,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Specific Mixer Routing Options based on active vehicle
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
                        text = "Configuración de Mezclador del Modelo",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    when (currentMode) {
                        DrivingMode.DUAL_DC, DrivingMode.ARCADE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Intercambiar Salidas A / B", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Asigna el canal izquierdo a B y el derecho a A", color = MutedText, fontSize = 9.sp)
                                }
                                CustomSwitch(
                                    checked = mixer.swapAB,
                                    onCheckedChange = { viewModel.updateMixer { m -> m.copy(swapAB = it) } }
                                )
                            }
                        }

                        DrivingMode.TANK -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Intercambiar Orugas (Swap Tracks)", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Invierte la oruga izquierda por la oruga derecha", color = MutedText, fontSize = 9.sp)
                                }
                                CustomSwitch(
                                    checked = mixer.swapTracks,
                                    onCheckedChange = { viewModel.updateMixer { m -> m.copy(swapTracks = it) } }
                                )
                            }
                        }

                        DrivingMode.SERVO_CAR -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Salida Física para Motor de Tracción", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Selecciona el borne del driver al que conectaste el motor", color = MutedText, fontSize = 9.sp)
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberSurfaceVariant)
                                ) {
                                    listOf("A", "B").forEach { out ->
                                        val selected = (mixer.servoMotorOutput == out)
                                        Box(
                                            modifier = Modifier
                                                .clickable { viewModel.updateMixer { m -> m.copy(servoMotorOutput = out) } }
                                                .background(if (selected) CyberPrimary else Color.Transparent)
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Canal $out",
                                                color = if (selected) Color.Black else OffWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
