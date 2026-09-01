package com.example.androidbljoy.ui.main

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.androidbljoy.AppTheme
import com.example.androidbljoy.MainActivity
import com.example.androidbljoy.data.BluetoothDeviceInfo
import com.example.androidbljoy.data.ConnectionStatus
import com.example.androidbljoy.data.UpdateInfo
import com.example.androidbljoy.data.UpdateManager
import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.theme.CyberBg
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSecondary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.NeonAmber
import com.example.androidbljoy.theme.NeonGreen
import com.example.androidbljoy.theme.NeonRed
import com.example.androidbljoy.theme.OffWhite
import com.example.androidbljoy.ui.tabs.ControlTab
import com.example.androidbljoy.ui.tabs.InputsTab
import com.example.androidbljoy.ui.tabs.MixerTab
import com.example.androidbljoy.ui.tabs.ModelsTab
import com.example.androidbljoy.ui.tabs.OutputsTab
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            val allGranted = permissionsMap.values.all { it }
            hasPermissions = allGranted
            if (allGranted) {
                viewModel.scanDevices()
            }
        }
    )

    // Check permissions on startup
    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        val allGranted = permissions.all {
            context.checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        hasPermissions = allGranted
        if (!allGranted) {
            permissionLauncher.launch(permissions)
        } else {
            viewModel.scanDevices()
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showConnectionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showModelPickerQuickDialog by remember { mutableStateOf(false) }

    // Update Manager state
    val updateManager = remember { UpdateManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingForUpdate by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var updateCheckResultMsg by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Auto-check for updates on startup
    LaunchedEffect(Unit) {
        try {
            val info = updateManager.checkForUpdates()
            if (info != null) {
                updateInfo = info
                showUpdateDialog = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val scannedDevices by viewModel.scannedDevices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val rssi by viewModel.rssi.collectAsStateWithLifecycle()

    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val allModels by viewModel.allModels.collectAsStateWithLifecycle()
    val autoLoadNotification by viewModel.autoLoadNotification.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR: RC Transmitter Header & Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(CyberSurface.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Bluetooth Status Pill & Quick Connect
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (connectionStatus != ConnectionStatus.CONNECTED) {
                                viewModel.scanDevices()
                                showConnectionDialog = true
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    val statusColor = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> NeonGreen
                        ConnectionStatus.CONNECTING -> NeonAmber
                        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> NeonRed
                    }

                    val transition = rememberInfiniteTransition(label = "pulse")
                    val alpha by transition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    val lightAlpha = if (connectionStatus == ConnectionStatus.CONNECTING) alpha else 1f

                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .alpha(lightAlpha)
                            .background(statusColor, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    val statusText = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> connectedDevice?.name ?: "Conectado"
                        ConnectionStatus.CONNECTING -> "Conectando..."
                        ConnectionStatus.DISCONNECTED -> "Desconectado"
                        ConnectionStatus.ERROR -> "Error BLE"
                    }

                    Text(
                        text = statusText,
                        color = OffWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val rssiVal = rssi
                        val rssiText = if (rssiVal != null) "$rssiVal dBm" else "-- dBm"
                        val rssiColor = when {
                            rssiVal == null -> MutedText
                            rssiVal >= -60 -> NeonGreen
                            rssiVal >= -80 -> NeonAmber
                            else -> NeonRed
                        }
                        Text(
                            text = rssiText,
                            color = rssiColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonRed.copy(alpha = 0.8f))
                                .clickable { viewModel.disconnectDevice() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("DESCONECTAR", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Center: Model Quick-Picker Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberSurfaceVariant.copy(alpha = 0.6f))
                        .clickable { showModelPickerQuickDialog = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MODELO: ",
                            color = MutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeModel.name,
                            color = CyberPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            color = CyberPrimary,
                            fontSize = 8.sp
                        )
                    }
                }

                // Right: 5 Tabs Navigation & Settings Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "🕹️ Control",
                        "🎮 Inputs",
                        "🔀 Mixer",
                        "⚡ Outputs",
                        "📁 Modelos"
                    )

                    tabs.forEachIndexed { index, title ->
                        val isSelected = (selectedTabIndex == index)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyberPrimary else Color.Transparent)
                                .clickable { selectedTabIndex = index }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.Black else OffWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = CyberPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Auto-load Notification Banner (Model Match Feedback)
            if (autoLoadNotification != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ $autoLoadNotification",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { viewModel.clearAutoLoadNotification() },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Tab Body Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> ControlTab(viewModel)
                    1 -> InputsTab(viewModel)
                    2 -> MixerTab(viewModel)
                    3 -> OutputsTab(viewModel)
                    4 -> ModelsTab(viewModel)
                }
            }
        }
    }

    // Quick Model Picker Dialog
    if (showModelPickerQuickDialog) {
        Dialog(onDismissRequest = { showModelPickerQuickDialog = false }) {
            Card(
                modifier = Modifier
                    .width(340.dp)
                    .border(1.5.dp, CyberPrimary, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Seleccionar Modelo Activo",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allModels, key = { it.id }) { model ->
                            val isCurrent = (model.id == activeModel.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) CyberPrimary else CyberSurfaceVariant)
                                    .clickable {
                                        viewModel.selectModel(model.id)
                                        showModelPickerQuickDialog = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = model.name,
                                    color = if (isCurrent) Color.Black else OffWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (model.vehicleType) {
                                        DrivingMode.DUAL_DC -> "Doble Motor"
                                        DrivingMode.TANK -> "Tanque"
                                        DrivingMode.SERVO_CAR -> "Servo"
                                        DrivingMode.ARCADE -> "Arcade"
                                    },
                                    color = if (isCurrent) Color.Black.copy(alpha = 0.7f) else MutedText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showModelPickerQuickDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Cerrar", color = OffWhite, fontSize = 10.sp)
                    }
                }
            }
        }
    }

    // Connection Dialog (Device Scanner & Picker)
    if (showConnectionDialog) {
        Dialog(onDismissRequest = {
            viewModel.stopScanning()
            showConnectionDialog = false
        }) {
            Card(
                modifier = Modifier
                    .width(420.dp)
                    .height(300.dp)
                    .border(2.dp, CyberPrimary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dispositivos BLE Disponibles",
                            color = CyberPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyberPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            IconButton(onClick = { viewModel.scanDevices() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refrescar",
                                    tint = OffWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!hasPermissions) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Permisos de Bluetooth y Ubicación requeridos.",
                                color = NeonRed,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Paired Devices list
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = "Vinculados",
                                    color = MutedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                if (pairedDevices.isEmpty()) {
                                    Text(
                                        text = "Ninguno",
                                        color = MutedText.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(pairedDevices) { device ->
                                            DeviceItem(device = device) {
                                                viewModel.connectDevice(device.address)
                                                showConnectionDialog = false
                                            }
                                        }
                                    }
                                }
                            }

                            // Discovered/Scanned Devices list
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = "Disponibles",
                                    color = MutedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                if (scannedDevices.isEmpty()) {
                                    Text(
                                        text = if (isScanning) "Buscando..." else "No encontrados",
                                        color = MutedText.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(scannedDevices) { device ->
                                            DeviceItem(device = device) {
                                                viewModel.connectDevice(device.address)
                                                showConnectionDialog = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                viewModel.stopScanning()
                                showConnectionDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant)
                        ) {
                            Text("Cerrar", color = OffWhite, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // System Settings Dialog (Themes & Updates)
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                modifier = Modifier
                    .width(380.dp)
                    .border(2.dp, CyberSecondary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ajustes del Sistema",
                        color = CyberSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Section: Theme Selection
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Personalización Visual",
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val activeTheme = MainActivity.appThemeState.value

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (activeTheme == AppTheme.CYBERPUNK) CyberPrimary else CyberSurfaceVariant)
                                        .clickable { MainActivity.appThemeState.value = AppTheme.CYBERPUNK }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Cyberpunk",
                                        color = if (activeTheme == AppTheme.CYBERPUNK) Color.Black else OffWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (activeTheme == AppTheme.RETRO_AMBER) CyberPrimary else CyberSurfaceVariant)
                                        .clickable { MainActivity.appThemeState.value = AppTheme.RETRO_AMBER }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Retro Amber",
                                        color = if (activeTheme == AppTheme.RETRO_AMBER) Color.Black else OffWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Section: System Updates
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Actualizaciones del Sistema",
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val currentVerName = updateManager.getCurrentVersionName()
                                val currentVerCode = updateManager.getCurrentVersionCode()

                                Column {
                                    Text(
                                        text = "Versión: v$currentVerName (code $currentVerCode)",
                                        color = OffWhite,
                                        fontSize = 10.sp
                                    )
                                    if (updateCheckResultMsg != null) {
                                        Text(
                                            text = updateCheckResultMsg ?: "",
                                            color = if (updateCheckResultMsg!!.contains("actualizada")) NeonGreen else NeonAmber,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isCheckingForUpdate = true
                                            updateCheckResultMsg = "Buscando..."
                                            val info = updateManager.checkForUpdates()
                                            isCheckingForUpdate = false
                                            if (info != null) {
                                                updateInfo = info
                                                showUpdateDialog = true
                                                updateCheckResultMsg = "¡Nueva versión!"
                                            } else {
                                                updateCheckResultMsg = "App actualizada."
                                            }
                                        }
                                    },
                                    enabled = !isCheckingForUpdate,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    if (isCheckingForUpdate) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            color = Color.Black,
                                            strokeWidth = 1.5.dp
                                        )
                                    } else {
                                        Text("Buscar", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showSettingsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Update Available Dialog
    if (showUpdateDialog && updateInfo != null) {
        val info = updateInfo!!
        Dialog(onDismissRequest = {
            if (!isDownloadingUpdate && !info.forceUpdate) {
                showUpdateDialog = false
            }
        }) {
            Card(
                modifier = Modifier
                    .width(380.dp)
                    .border(2.dp, CyberPrimary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Actualización Disponible",
                        color = CyberPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Nueva Versión: v${info.versionName} (code ${info.versionCode})",
                            color = OffWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Notas de los cambios:",
                            color = MutedText,
                            fontSize = 9.sp
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(6.dp)) {
                                item {
                                    Text(
                                        text = info.releaseNotes,
                                        color = OffWhite,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isDownloadingUpdate) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = CyberPrimary,
                                trackColor = CyberSurfaceVariant
                            )
                            Text(
                                text = "Descargando: ${(downloadProgress * 100).toInt()}%",
                                color = CyberPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!info.forceUpdate) {
                                Button(
                                    onClick = { showUpdateDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Más tarde", color = OffWhite, fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isDownloadingUpdate = true
                                        downloadProgress = 0f
                                        val file = updateManager.downloadApk(info.apkUrl) { progress ->
                                            downloadProgress = progress
                                        }
                                        isDownloadingUpdate = false
                                        if (file != null) {
                                            updateManager.installApk(file)
                                            showUpdateDialog = false
                                        } else {
                                            updateCheckResultMsg = "Error al descargar actualización"
                                            showUpdateDialog = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Actualizar ya", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: BluetoothDeviceInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                color = OffWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = device.address,
                color = MutedText,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
