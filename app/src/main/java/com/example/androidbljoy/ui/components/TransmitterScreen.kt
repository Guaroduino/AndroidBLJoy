package com.example.androidbljoy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbljoy.data.ConnectionStatus
import com.example.androidbljoy.data.model.RcModelConfig
import com.example.androidbljoy.theme.CyberBg
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.NeonAmber
import com.example.androidbljoy.theme.NeonGreen
import com.example.androidbljoy.theme.NeonRed
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.ui.main.MainScreenViewModel
import com.example.androidbljoy.ui.tabs.InputsTab
import com.example.androidbljoy.ui.tabs.MixerTab
import com.example.androidbljoy.ui.tabs.ModelsTab
import com.example.androidbljoy.ui.tabs.OutputsTab

@Composable
fun TransmitterScreen(
    viewModel: MainScreenViewModel,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onOpenModelPicker: () -> Unit,
    onOpenConnection: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val isTestMode by viewModel.isTestMode.collectAsStateWithLifecycle()
    val autoLoadNotification by viewModel.autoLoadNotification.collectAsStateWithLifecycle()

    val motorA by viewModel.outputMotorA.collectAsStateWithLifecycle()
    val motorB by viewModel.outputMotorB.collectAsStateWithLifecycle()
    val servoAngle by viewModel.outputServo.collectAsStateWithLifecycle()
    val lastSentMessage by viewModel.lastSentMessage.collectAsStateWithLifecycle()

    // Outer Realistic Screen Bezel
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070A0E))
            .border(2.dp, CyberPrimary.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0B0F15))
        ) {
            // 1. EdgeTX Status Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(BorderStroke(0.5.dp, CyberSurfaceVariant))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Active Model Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberSurfaceVariant.copy(alpha = 0.7f))
                        .clickable { onOpenModelPicker() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("MDL:", color = MutedText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = activeModel.name,
                        color = CyberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("▼", color = CyberPrimary, fontSize = 7.sp)
                }

                // Center: BLE Connection Status Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                connectionStatus == ConnectionStatus.CONNECTED -> NeonGreen.copy(alpha = 0.15f)
                                connectionStatus == ConnectionStatus.CONNECTING || isScanning -> NeonAmber.copy(alpha = 0.15f)
                                else -> CyberSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                connectionStatus == ConnectionStatus.CONNECTED -> NeonGreen.copy(alpha = 0.5f)
                                connectionStatus == ConnectionStatus.CONNECTING || isScanning -> NeonAmber.copy(alpha = 0.5f)
                                else -> MutedText.copy(alpha = 0.25f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onOpenConnection() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Pulsing/status dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    connectionStatus == ConnectionStatus.CONNECTED -> NeonGreen
                                    connectionStatus == ConnectionStatus.CONNECTING || isScanning -> NeonAmber
                                    else -> NeonRed
                                }
                            )
                    )
                    Text(
                        text = when {
                            connectionStatus == ConnectionStatus.CONNECTED -> connectedDevice?.name ?: "ENLAZADO"
                            connectionStatus == ConnectionStatus.CONNECTING -> "CONECTANDO..."
                            isScanning -> "BUSCANDO..."
                            else -> "DESCONECTADO"
                        },
                        color = when {
                            connectionStatus == ConnectionStatus.CONNECTED -> NeonGreen
                            connectionStatus == ConnectionStatus.CONNECTING || isScanning -> NeonAmber
                            else -> OffWhite
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Right Status: Simulation / Battery / Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Test / Simulation Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isTestMode) NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.toggleTestMode() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isTestMode) "🧪 SIM ON" else "SIM OFF",
                            color = if (isTestMode) NeonGreen else MutedText.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Battery / Telemetry
                    Text(
                        text = "🔋 4.2V",
                        color = CyberPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Settings Icon
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = CyberPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onOpenSettings() }
                    )
                }
            }

            // 2. Integrated LCD Navigation Tabs (EdgeTX style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(Color(0xFF090D13))
                    .border(BorderStroke(0.5.dp, CyberSurfaceVariant)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    "DASH",
                    "INPUTS",
                    "MIXER",
                    "OUTPUTS",
                    "MODEL"
                )

                tabs.forEachIndexed { index, title ->
                    val isSelected = (selectedTabIndex == index)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isSelected) CyberPrimary.copy(alpha = 0.25f) else Color.Transparent)
                            .border(
                                BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) CyberPrimary else Color.Transparent
                                )
                            )
                            .clickable { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) CyberPrimary else MutedText,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Notification Banner if active
            if (autoLoadNotification != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ $autoLoadNotification",
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = NeonGreen,
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { viewModel.clearAutoLoadNotification() }
                    )
                }
            }

            // 3. Screen Viewport / Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(2.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // DASH: Vehicle Visualizer & Realtime Telemetry HUD
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VehicleVisualizer(
                                vehicleType = activeModel.vehicleType,
                                motorA = motorA,
                                motorB = motorB,
                                servoAngle = servoAngle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "GATT: $lastSentMessage",
                                        color = CyberPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "M1:$motorA M2:$motorB S:$servoAngle°",
                                        color = OffWhite.copy(alpha = 0.7f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                    1 -> InputsTab(viewModel)
                    2 -> MixerTab(viewModel)
                    3 -> OutputsTab(viewModel)
                    4 -> ModelsTab(viewModel)
                }
            }
        }
    }
}
