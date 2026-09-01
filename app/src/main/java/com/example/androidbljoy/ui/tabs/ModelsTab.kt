package com.example.androidbljoy.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbljoy.data.ConnectionStatus
import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSecondary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.NeonGreen
import com.example.androidbljoy.theme.NeonRed
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.ui.components.CustomSwitch
import com.example.androidbljoy.ui.main.MainScreenViewModel

@Composable
fun ModelsTab(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val allModels by viewModel.allModels.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    var showCreateModelDialog by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Active Model Card with Model Match settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.9f)),
                border = BorderStroke(1.5.dp, CyberPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header with Active Model Name and edit button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEditingName) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = { Text("Nombre del Modelo", color = CyberPrimary, fontSize = 9.sp) },
                                    textStyle = TextStyle(color = OffWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberPrimary,
                                        unfocusedBorderColor = CyberSurfaceVariant
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).height(50.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (editedName.isNotBlank()) {
                                            viewModel.setModelName(editedName)
                                        }
                                        isEditingName = false
                                    }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Guardar", tint = NeonGreen)
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeModel.name,
                                    color = CyberPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        editedName = activeModel.name
                                        isEditingName = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Renombrar", tint = MutedText, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Vehicle type chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when (activeModel.vehicleType) {
                                    DrivingMode.DUAL_DC -> "DOBLE MOTOR"
                                    DrivingMode.TANK -> "TANQUE"
                                    DrivingMode.SERVO_CAR -> "COCHE RC"
                                    DrivingMode.ARCADE -> "ARCADE"
                                },
                                color = CyberPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Model Match Bluetooth Binding Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Model Match (Vinculación BLE)",
                                color = CyberSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Vincula este perfil a la dirección MAC de tu vehículo para que se cargue automáticamente al conectar.",
                                color = MutedText,
                                fontSize = 9.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val linkedMac = activeModel.linkedDeviceAddress
                                    val linkedName = activeModel.linkedDeviceName
                                    if (linkedMac != null) {
                                        Text(
                                            text = "Vinculado a: ${linkedName ?: "Dispositivo"} ($linkedMac)",
                                            color = NeonGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text(
                                            text = "Sin vehículo vinculado",
                                            color = MutedText,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (activeModel.linkedDeviceAddress != null) {
                                    Button(
                                        onClick = { viewModel.unlinkDeviceFromActiveModel() },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Desvincular", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    val isConnected = (connectionStatus == ConnectionStatus.CONNECTED && connectedDevice != null)
                                    Button(
                                        onClick = { viewModel.linkCurrentConnectedDevice() },
                                        enabled = isConnected,
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isConnected) "Vincular a ${connectedDevice?.name ?: "Actual"}" else "Conecta BLE para vincular",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Cargar automáticamente al conectar este vehículo", color = OffWhite, fontSize = 10.sp)
                                CustomSwitch(
                                    checked = activeModel.autoLoadOnConnect,
                                    onCheckedChange = { viewModel.toggleAutoLoadOnConnect() }
                                )
                            }
                        }
                    }

                    // Duplicate and Delete active model buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.duplicateActiveModel() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                            modifier = Modifier.weight(1f).height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("Duplicar Modelo", color = OffWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.deleteActiveModel() },
                            enabled = allModels.size > 1,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f).height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("Eliminar Modelo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Saved Models List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modelos Guardados (${allModels.size})",
                    color = CyberPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showCreateModelDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Modelo", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(allModels, key = { it.id }) { model ->
            val isCurrent = (model.id == activeModel.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectModel(model.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) CyberPrimary.copy(alpha = 0.15f) else CyberSurface.copy(alpha = 0.7f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) CyberPrimary else CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = model.name,
                            color = if (isCurrent) CyberPrimary else OffWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = when (model.vehicleType) {
                                    DrivingMode.DUAL_DC -> "Doble Motor"
                                    DrivingMode.TANK -> "Tanque"
                                    DrivingMode.SERVO_CAR -> "Coche Servo"
                                    DrivingMode.ARCADE -> "Arcade"
                                },
                                color = MutedText,
                                fontSize = 9.sp
                            )
                            if (model.linkedDeviceAddress != null) {
                                Text(
                                    text = "• Vinculado: ${model.linkedDeviceName ?: model.linkedDeviceAddress}",
                                    color = NeonGreen.copy(alpha = 0.8f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVO", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Create New Model Dialog
    if (showCreateModelDialog) {
        var newName by remember { mutableStateOf("") }
        var selectedMode by remember { mutableStateOf(DrivingMode.DUAL_DC) }

        Dialog(onDismissRequest = { showCreateModelDialog = false }) {
            Card(
                modifier = Modifier
                    .width(360.dp)
                    .border(1.5.dp, CyberPrimary, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Crear Nuevo Modelo RC",
                        color = CyberPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre del Modelo", color = CyberPrimary, fontSize = 10.sp) },
                        placeholder = { Text("Ej. Mi Crawler 4x4", color = MutedText, fontSize = 11.sp) },
                        textStyle = TextStyle(color = OffWhite, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberSurfaceVariant
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo de Vehículo:", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DrivingMode.values().forEach { mode ->
                            val isSel = (selectedMode == mode)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) CyberPrimary else CyberSurfaceVariant)
                                    .clickable { selectedMode = mode }
                                    .padding(vertical = 6.dp, horizontal = 10.dp)
                            ) {
                                Text(
                                    text = when (mode) {
                                        DrivingMode.DUAL_DC -> "Doble Motor DC"
                                        DrivingMode.TANK -> "Tanque Oruga"
                                        DrivingMode.SERVO_CAR -> "Coche RC + Servo de Dirección"
                                        DrivingMode.ARCADE -> "Arcade Diferencial"
                                    },
                                    color = if (isSel) Color.Black else OffWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showCreateModelDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Cancelar", color = OffWhite, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.createNewModel(newName, selectedMode)
                                showCreateModelDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Crear", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
