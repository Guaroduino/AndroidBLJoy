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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.androidbljoy.data.UpdateManager
import com.example.androidbljoy.data.UpdateInfo
import com.example.androidbljoy.MainActivity
import com.example.androidbljoy.AppTheme
import kotlinx.coroutines.launch

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.androidbljoy.data.BluetoothDeviceInfo
import com.example.androidbljoy.data.BluetoothService
import com.example.androidbljoy.data.ConnectionStatus
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
import com.example.androidbljoy.ui.components.Joystick
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput

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

    var showConnectionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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

    val drivingMode by viewModel.drivingMode.collectAsStateWithLifecycle()
    val tractionLimit by viewModel.tractionLimit.collectAsStateWithLifecycle()
    val steeringLimit by viewModel.steeringLimit.collectAsStateWithLifecycle()
    val lastSentMessage by viewModel.lastSentMessage.collectAsStateWithLifecycle()

    val tractionVal by viewModel.tractionValue.collectAsStateWithLifecycle()
    val steeringVal by viewModel.steeringValue.collectAsStateWithLifecycle()

    val tractionTrim by viewModel.tractionTrim.collectAsStateWithLifecycle()
    val tractionTrimLocked by viewModel.tractionTrimLocked.collectAsStateWithLifecycle()
    val steeringTrim by viewModel.steeringTrim.collectAsStateWithLifecycle()
    val steeringTrimLocked by viewModel.steeringTrimLocked.collectAsStateWithLifecycle()

    val swapJoysticks by viewModel.swapJoysticks.collectAsStateWithLifecycle()
    val unifiedJoystick by viewModel.unifiedJoystick.collectAsStateWithLifecycle()
    val tractionHardwareMode by viewModel.tractionHardwareMode.collectAsStateWithLifecycle()

    val tractionExpo by viewModel.tractionExpo.collectAsStateWithLifecycle()
    val steeringExpo by viewModel.steeringExpo.collectAsStateWithLifecycle()
    val deadbandA by viewModel.deadbandA.collectAsStateWithLifecycle()
    val deadbandB by viewModel.deadbandB.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberBg
    ) {
        val leftColumnContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
            if (unifiedJoystick && drivingMode != DrivingMode.TANK) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(2.4f).fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                val leftJoyLabel = if (drivingMode == DrivingMode.TANK) "Oruga Izq (Y)" else "Tracción (Y)"
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.weight(2.4f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Joystick(
                            name = leftJoyLabel,
                            isVerticalOnly = true,
                            externalY = tractionVal,
                            onValueChanged = { _, y -> viewModel.updateTraction(y) }
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                        ) {
                            IconButton(onClick = { viewModel.toggleTractionTrimLock() }) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Trim",
                                    tint = if (tractionTrimLocked) NeonRed else MutedText.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${if (tractionTrim > 0) "+" else ""}$tractionTrim",
                                color = OffWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.height(220.dp).width(36.dp)
                            ) {
                                Slider(
                                    value = tractionTrim.toFloat(),
                                    onValueChange = { viewModel.setTractionTrim(it.toInt()) },
                                    valueRange = -50f..50f,
                                    enabled = !tractionTrimLocked,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (!tractionTrimLocked) CyberPrimary else MutedText,
                                        activeTrackColor = if (!tractionTrimLocked) CyberPrimary else MutedText.copy(alpha=0.5f),
                                        inactiveTrackColor = CyberSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .requiredWidth(220.dp)
                                        .graphicsLayer {
                                            rotationZ = -90f
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                                        }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PressHoldButton(
                            text = "▲ Avanzar",
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            onPressed = { viewModel.updateTraction(1.0f) },
                            onReleased = { viewModel.updateTraction(0f) }
                        )
                        PressHoldButton(
                            text = "▼ Retroceder",
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
                Spacer(modifier = Modifier.weight(1f).fillMaxHeight(0.9f))
            } else {
                val rightJoyLabel = if (drivingMode == DrivingMode.TANK) "Oruga Der (Y)" else "Dirección (X)"
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.weight(2.4f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Joystick(
                            name = rightJoyLabel,
                            isVerticalOnly = (drivingMode == DrivingMode.TANK),
                            isHorizontalOnly = (drivingMode != DrivingMode.TANK),
                            isDigital = false,
                            externalX = if (drivingMode != DrivingMode.TANK) steeringVal else null,
                            externalY = if (drivingMode == DrivingMode.TANK) steeringVal else null,
                            onValueChanged = { x, y -> 
                                if (drivingMode == DrivingMode.TANK) {
                                    viewModel.updateSteering(y)
                                } else {
                                    viewModel.updateSteering(x)
                                }
                            }
                        )

                        if (drivingMode != DrivingMode.DUAL_DC && drivingMode != DrivingMode.TANK) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                IconButton(onClick = { viewModel.toggleSteeringTrimLock() }) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock Trim",
                                        tint = if (steeringTrimLocked) NeonRed else MutedText.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "${if (steeringTrim > 0) "+" else ""}$steeringTrim",
                                    color = OffWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                Slider(
                                    value = steeringTrim.toFloat(),
                                    onValueChange = { viewModel.setSteeringTrim(it.toInt()) },
                                    valueRange = -50f..50f,
                                    enabled = !steeringTrimLocked,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (!steeringTrimLocked) CyberPrimary else MutedText,
                                        activeTrackColor = if (!steeringTrimLocked) CyberPrimary else MutedText.copy(alpha=0.5f),
                                        inactiveTrackColor = CyberSurfaceVariant
                                    ),
                                    modifier = Modifier.width(180.dp).height(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (drivingMode == DrivingMode.TANK) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.6f)
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (swapJoysticks) {
                rightColumnContent()
            } else {
                leftColumnContent()
            }

            // CENTER COLUMN: Header, Driving Mode Selection, Configurations, Telemetry Stream
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Transmisor RC Pro",
                        color = CyberPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Bluetooth Low Energy (BLE)",
                        color = MutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Main Config Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Bluetooth connection status row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val statusColor = when (connectionStatus) {
                                ConnectionStatus.CONNECTED -> NeonGreen
                                ConnectionStatus.CONNECTING -> NeonAmber
                                ConnectionStatus.DISCONNECTED -> NeonRed
                                ConnectionStatus.ERROR -> NeonRed
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
                                    .size(8.dp)
                                    .alpha(lightAlpha)
                                    .background(statusColor, CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            val statusText = when (connectionStatus) {
                                ConnectionStatus.CONNECTED -> "Conectado"
                                ConnectionStatus.CONNECTING -> "Conectando"
                                ConnectionStatus.DISCONNECTED -> "Desconectado"
                                ConnectionStatus.ERROR -> "Error"
                            }

                            Text(
                                text = "Estado: $statusText",
                                color = OffWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (connectionStatus == ConnectionStatus.CONNECTED) {
                                Spacer(modifier = Modifier.width(12.dp))
                                val rssiVal = rssi
                                val rssiText = if (rssiVal != null) "$rssiVal dBm" else "-- dBm"
                                val rssiColor = when {
                                    rssiVal == null -> MutedText
                                    rssiVal >= -60 -> NeonGreen
                                    rssiVal >= -80 -> NeonAmber
                                    else -> NeonRed
                                }
                                Text(
                                    text = "RSSI: $rssiText",
                                    color = rssiColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Driving Mode Tabs Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberSurfaceVariant),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DrivingMode.values().forEach { mode ->
                                val label = when (mode) {
                                    DrivingMode.DUAL_DC -> "Doble Motor"
                                    DrivingMode.TANK -> "Tanque"
                                    DrivingMode.SERVO_CAR -> "Servo"
                                    DrivingMode.ARCADE -> "Arcade"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setDrivingMode(mode) }
                                        .background(if (drivingMode == mode) CyberPrimary else Color.Transparent)
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (drivingMode == mode) Color.Black else OffWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Dynamic Configuration Panel per Mode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            when (drivingMode) {
                                DrivingMode.DUAL_DC -> {
                                    val invertTrac by viewModel.invertTraction.collectAsStateWithLifecycle()
                                    val invertSteer by viewModel.invertSteering.collectAsStateWithLifecycle()
                                    val swapAB by viewModel.swapAB.collectAsStateWithLifecycle()

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Invertir Tracción", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = invertTrac, onCheckedChange = { viewModel.setInvertTraction(it) })
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Invertir Dirección", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = invertSteer, onCheckedChange = { viewModel.setInvertSteering(it) })
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Swap Salidas (A/B)", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = swapAB, onCheckedChange = { viewModel.setSwapAB(it) })
                                        }

                                    }
                                }
                                DrivingMode.TANK -> {
                                    val invertL by viewModel.invertLeftTrack.collectAsStateWithLifecycle()
                                    val invertR by viewModel.invertRightTrack.collectAsStateWithLifecycle()
                                    val swapT by viewModel.swapTracks.collectAsStateWithLifecycle()

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Invertir Oruga Izq.", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = invertL, onCheckedChange = { viewModel.setInvertLeftTrack(it) })
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Invertir Oruga Der.", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = invertR, onCheckedChange = { viewModel.setInvertRightTrack(it) })
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Swap Orugas (A/B)", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = swapT, onCheckedChange = { viewModel.setSwapTracks(it) })
                                        }
                                    }
                                }
                                DrivingMode.SERVO_CAR -> {
                                    val motorOut by viewModel.servoMotorOutput.collectAsStateWithLifecycle()
                                    val invertTracServo by viewModel.invertTractionServo.collectAsStateWithLifecycle()
                                    val invertServo by viewModel.invertServo.collectAsStateWithLifecycle()
                                    val trim by viewModel.trimSteering.collectAsStateWithLifecycle()
                                    val epa by viewModel.epaSteering.collectAsStateWithLifecycle()

                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Salida Motor", color = OffWhite, fontSize = 10.sp)
                                            Row(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(CyberSurfaceVariant)) {
                                                listOf("A", "B").forEach { out ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clickable { viewModel.setServoMotorOutput(out) }
                                                            .background(if (motorOut == out) CyberPrimary else Color.Transparent)
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(out, color = if (motorOut == out) Color.Black else OffWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Invertir Trac/Servo", color = OffWhite, fontSize = 10.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Trac", color = MutedText, fontSize = 9.sp)
                                                CustomSwitch(checked = invertTracServo, onCheckedChange = { viewModel.setInvertTractionServo(it) })
                                                Text("Servo", color = MutedText, fontSize = 9.sp)
                                                CustomSwitch(checked = invertServo, onCheckedChange = { viewModel.setInvertServo(it) })
                                            }
                                        }
                                        // Trim Slider
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Trim Dirección", color = OffWhite, fontSize = 9.sp)
                                                Text("${if (trim >= 0) "+" else ""}$trim°", color = CyberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = trim.toFloat(),
                                                onValueChange = { viewModel.setTrimSteering(it.toInt()) },
                                                valueRange = -45f..45f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = CyberPrimary,
                                                    activeTrackColor = CyberPrimary,
                                                    inactiveTrackColor = CyberSurfaceVariant
                                                ),
                                                modifier = Modifier.height(18.dp)
                                            )
                                        }
                                        // EPA Slider
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("EPA Dirección", color = OffWhite, fontSize = 9.sp)
                                                Text("$epa%", color = CyberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = epa.toFloat(),
                                                onValueChange = { viewModel.setEpaSteering(it.toInt()) },
                                                valueRange = 10f..100f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = CyberPrimary,
                                                    activeTrackColor = CyberPrimary,
                                                    inactiveTrackColor = CyberSurfaceVariant
                                                ),
                                                modifier = Modifier.height(18.dp)
                                            )
                                        }
                                    }
                                }
                                DrivingMode.ARCADE -> {
                                    val invertTrac by viewModel.invertTraction.collectAsStateWithLifecycle()
                                    val invertSteer by viewModel.invertSteering.collectAsStateWithLifecycle()
                                    val swapAB by viewModel.swapAB.collectAsStateWithLifecycle()

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Invertir Aceleración", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = invertTrac, onCheckedChange = { viewModel.setInvertTraction(it) })
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Invertir Dirección", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = invertSteer, onCheckedChange = { viewModel.setInvertSteering(it) })
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Swap Motores (A/B)", color = OffWhite, fontSize = 11.sp)
                                            CustomSwitch(checked = swapAB, onCheckedChange = { viewModel.setSwapAB(it) })
                                        }
                                    }
                                }
                            }
                        }

                        // Connection control / selector button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (connectionStatus == ConnectionStatus.CONNECTED) {
                                Button(
                                    onClick = { viewModel.disconnectDevice() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Desconectar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.scanDevices()
                                        showConnectionDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Conectar BLE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.toggleSwapJoysticks() },
                                modifier = Modifier
                                    .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                    .size(36.dp)
                            ) {
                                Text(
                                    text = "⇄",
                                    color = CyberPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { showSettingsDialog = true },
                                modifier = Modifier
                                    .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuración",
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Data Stream Monitor Panel (GATT format telemetry payload output)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, CyberSecondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GATT: $lastSentMessage",
                            color = CyberSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "L:$tractionVal R:$steeringVal",
                            color = OffWhite.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            if (swapJoysticks) {
                leftColumnContent()
            } else {
                rightColumnContent()
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dispositivos BLE Disponibles",
                            color = CyberPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
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

                    Spacer(modifier = Modifier.height(8.dp))

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
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                        text = if (isScanning) "Escaneando..." else "No encontrados",
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                            Text("Cerrar", color = OffWhite)
                        }
                    }
                }
            }
        }
    }

    // Extra Settings Dialog
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                modifier = Modifier
                    .width(420.dp)
                    .border(2.dp, CyberSecondary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Configuración del Sistema",
                        color = CyberSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Scrollable/grouped list to allow adding more items in the future
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Section: Curvas y Límites (EPA, Expo, Deadband)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "EPA y Expo (Ajustes de Curva)",
                                        color = CyberPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    // EPA Row
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = if (tractionLimit == 0) "" else tractionLimit.toString(),
                                            onValueChange = {
                                                val limit = it.toIntOrNull() ?: 0
                                                viewModel.setTractionLimit(limit)
                                            },
                                            label = { Text("EPA Tracción", fontSize = 8.sp, color = MutedText) },
                                            textStyle = TextStyle(color = OffWhite, fontSize = 10.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyberPrimary,
                                                unfocusedBorderColor = CyberSurfaceVariant,
                                                focusedLabelColor = CyberPrimary
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = if (steeringLimit == 0) "" else steeringLimit.toString(),
                                            onValueChange = {
                                                val limit = it.toIntOrNull() ?: 0
                                                viewModel.setSteeringLimit(limit)
                                            },
                                            label = { Text("EPA Dirección", fontSize = 8.sp, color = MutedText) },
                                            textStyle = TextStyle(color = OffWhite, fontSize = 10.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyberPrimary,
                                                unfocusedBorderColor = CyberSurfaceVariant,
                                                focusedLabelColor = CyberPrimary
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Expo Sliders
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Expo Tracción", color = OffWhite, fontSize = 9.sp)
                                            Text("$tractionExpo%", color = CyberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = tractionExpo.toFloat(),
                                            onValueChange = { viewModel.setTractionExpo(it.toInt()) },
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberPrimary,
                                                activeTrackColor = CyberPrimary,
                                                inactiveTrackColor = CyberSurfaceVariant
                                            ),
                                            modifier = Modifier.height(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Expo Dirección", color = OffWhite, fontSize = 9.sp)
                                            Text("$steeringExpo%", color = CyberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = steeringExpo.toFloat(),
                                            onValueChange = { viewModel.setSteeringExpo(it.toInt()) },
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberPrimary,
                                                activeTrackColor = CyberPrimary,
                                                inactiveTrackColor = CyberSurfaceVariant
                                            ),
                                            modifier = Modifier.height(18.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Deadband (Zona Muerta del Motor)",
                                        color = CyberPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // Deadband Sliders
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Deadband Motor A", color = OffWhite, fontSize = 9.sp)
                                            Text("$deadbandA", color = CyberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = deadbandA.toFloat(),
                                            onValueChange = { viewModel.setDeadbandA(it.toInt()) },
                                            valueRange = 0f..127f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberPrimary,
                                                activeTrackColor = CyberPrimary,
                                                inactiveTrackColor = CyberSurfaceVariant
                                            ),
                                            modifier = Modifier.height(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Deadband Motor B", color = OffWhite, fontSize = 9.sp)
                                            Text("$deadbandB", color = CyberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = deadbandB.toFloat(),
                                            onValueChange = { viewModel.setDeadbandB(it.toInt()) },
                                            valueRange = 0f..127f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberPrimary,
                                                activeTrackColor = CyberPrimary,
                                                inactiveTrackColor = CyberSurfaceVariant
                                            ),
                                            modifier = Modifier.height(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Section: Joystick Options
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Opciones de Joystick",
                                        color = CyberPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Joystick Unificado (Una Mano)", color = OffWhite, fontSize = 11.sp)
                                        CustomSwitch(checked = unifiedJoystick, onCheckedChange = { viewModel.toggleUnifiedJoystick() })
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (unifiedJoystick) "Posición en la Derecha" else "Intercambiar Joysticks", color = OffWhite, fontSize = 11.sp)
                                        CustomSwitch(checked = swapJoysticks, onCheckedChange = { viewModel.toggleSwapJoysticks() })
                                    }
                                }
                            }
                        }

                        // Section: Hardware Mode
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Hardware de Tracción",
                                        color = CyberPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.setTractionHardwareMode(0) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (tractionHardwareMode == 0) CyberPrimary else CyberSurfaceVariant,
                                                contentColor = if (tractionHardwareMode == 0) Color.Black else OffWhite
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                        ) {
                                            Text("Driver DC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.setTractionHardwareMode(1) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (tractionHardwareMode == 1) CyberPrimary else CyberSurfaceVariant,
                                                contentColor = if (tractionHardwareMode == 1) Color.Black else OffWhite
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                        ) {
                                            Text("ESC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Updates
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                                    updateCheckResultMsg = "Buscando actualización..."
                                                    val info = updateManager.checkForUpdates()
                                                    isCheckingForUpdate = false
                                                    if (info != null) {
                                                        updateInfo = info
                                                        showUpdateDialog = true
                                                        updateCheckResultMsg = "¡Nueva versión disponible!"
                                                    } else {
                                                        updateCheckResultMsg = "Aplicación actualizada."
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
                        }

                        // Section: Theme Selection
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
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
                                        
                                        // Option 1: Cyberpunk Neon
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

                                        // Option 2: Retro Amber
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
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp, 18.dp)
            .clip(CircleShape)
            .background(if (checked) CyberPrimary else CyberSurfaceVariant)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (checked) Color.Black else MutedText)
        )
    }
}

@Composable
fun PressHoldButton(
    text: String,
    modifier: Modifier = Modifier,
    onPressed: () -> Unit,
    onReleased: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) CyberPrimary else CyberSurfaceVariant)
            .border(1.dp, if (isPressed) Color.White else CyberPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .pointerInput(onPressed, onReleased) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onPressed()
                    waitForUpOrCancellation()
                    isPressed = false
                    onReleased()
                }
            }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPressed) Color.Black else OffWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
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

