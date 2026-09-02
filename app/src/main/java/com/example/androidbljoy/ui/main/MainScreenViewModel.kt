package com.example.androidbljoy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbljoy.data.BluetoothDeviceInfo
import com.example.androidbljoy.data.BluetoothService
import com.example.androidbljoy.data.ConnectionStatus
import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.data.model.InputsConfig
import com.example.androidbljoy.data.model.MixerConfig
import com.example.androidbljoy.data.model.OutputsConfig
import com.example.androidbljoy.data.model.RcModelConfig
import com.example.androidbljoy.data.repository.ModelRepository
import com.example.androidbljoy.ui.components.calculateExpo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    val bluetoothService: BluetoothService,
    private val modelRepository: ModelRepository
) : ViewModel() {

    // Connection and Scanner flows
    val connectionStatus: StateFlow<ConnectionStatus> = bluetoothService.connectionStatus
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = bluetoothService.connectedDevice
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = bluetoothService.pairedDevices
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>> = bluetoothService.scannedDevices
    val isScanning: StateFlow<Boolean> = bluetoothService.isScanning
    val rssi: StateFlow<Int?> = bluetoothService.rssi

    // Model Repository bindings
    val activeModel: StateFlow<RcModelConfig> = modelRepository.activeModel
    val allModels: StateFlow<List<RcModelConfig>> = modelRepository.models

    // Banner notification for automatic model loading (Model Match)
    private val _autoLoadNotification = MutableStateFlow<String?>(null)
    val autoLoadNotification: StateFlow<String?> = _autoLoadNotification.asStateFlow()

    // Live raw stick inputs (-1.0f to 1.0f)
    val tractionValue = MutableStateFlow(0f)
    val steeringValue = MutableStateFlow(0f)

    // Calculated outputs sent to hardware
    val outputMotorA = MutableStateFlow(0)
    val outputMotorB = MutableStateFlow(0)
    val outputServo = MutableStateFlow(90)

    // Trim locks for on-screen quick sliders
    private val _tractionTrimLocked = MutableStateFlow(false)
    val tractionTrimLocked: StateFlow<Boolean> = _tractionTrimLocked.asStateFlow()

    private val _steeringTrimLocked = MutableStateFlow(false)
    val steeringTrimLocked: StateFlow<Boolean> = _steeringTrimLocked.asStateFlow()

    // Test / Simulation Mode (allows testing joysticks, mixers, visualizer without BLE robot)
    private val _isTestMode = MutableStateFlow(false)
    val isTestMode: StateFlow<Boolean> = _isTestMode.asStateFlow()

    fun toggleTestMode() {
        _isTestMode.value = !_isTestMode.value
    }

    // Telemetry feedback of last message
    private val _lastSentMessage = MutableStateFlow("SIM: A,0,B,0,S,90,M,0\\n")
    val lastSentMessage: StateFlow<String> = _lastSentMessage.asStateFlow()

    private var transmissionJob: Job? = null
    private var connectionObserverJob: Job? = null

    init {
        startTransmissionLoop()
        observeConnectionForModelMatch()
    }

    private fun startTransmissionLoop() {
        transmissionJob?.cancel()
        transmissionJob = viewModelScope.launch {
            while (true) {
                // Real-time calculation loop runs continuously for instant UI visualizer and telemetry updates
                calculateAndSend()
                delay(20L) // 50Hz refresh rate
            }
        }
    }

    /**
     * Watches for BLE connection events and applies Model Match auto-binding
     */
    private fun observeConnectionForModelMatch() {
        connectionObserverJob?.cancel()
        connectionObserverJob = viewModelScope.launch {
            var previousStatus = connectionStatus.value
            connectionStatus.collect { currentStatus ->
                if (currentStatus == ConnectionStatus.CONNECTED && previousStatus != ConnectionStatus.CONNECTED) {
                    val dev = connectedDevice.value
                    if (dev != null) {
                        val matched = modelRepository.findModelForBleDevice(dev.address)
                        if (matched != null && matched.id != activeModel.value.id) {
                            modelRepository.selectModel(matched.id)
                            _autoLoadNotification.value = "Modelo '${matched.name}' cargado automáticamente"
                        }
                    }
                }
                previousStatus = currentStatus
            }
        }
    }

    fun clearAutoLoadNotification() {
        _autoLoadNotification.value = null
    }

    private fun applyStickDeadzone(input: Float, deadzone: Float): Float {
        val absVal = kotlin.math.abs(input)
        if (absVal <= deadzone) return 0f
        val sign = if (input > 0f) 1f else -1f
        val denominator = (1f - deadzone).coerceAtLeast(0.001f)
        return (sign * ((absVal - deadzone) / denominator)).coerceIn(-1f, 1f)
    }

    companion object {
        fun mapDemandToEpa(demand: Float, minEpa: Int, maxEpa: Int): Int {
            if (demand == 0f) return 0

            return if (minEpa < 0 && maxEpa > 0) {
                // Bidirectional with asymmetric limits (e.g. -100 to +180 or -255 to +255)
                if (demand > 0f) {
                    (demand * maxEpa).toInt()
                } else {
                    (kotlin.math.abs(demand) * minEpa).toInt()
                }
            } else if (minEpa >= 0 && maxEpa >= 0) {
                // Strictly positive band (e.g. 20 to 240) -> excludes negatives
                if (demand > 0f) {
                    (minEpa + demand * (maxEpa - minEpa)).toInt()
                } else {
                    0
                }
            } else if (minEpa <= 0 && maxEpa <= 0) {
                // Strictly negative band (e.g. -240 to -20) -> excludes positives
                if (demand < 0f) {
                    val absD = kotlin.math.abs(demand)
                    (maxEpa - absD * (maxEpa - minEpa)).toInt()
                } else {
                    0
                }
            } else {
                0
            }
        }
    }

    private fun applyDeadband(motorValue: Int, deadband: Int): Int {
        if (motorValue == 0) return 0
        val sign = if (motorValue > 0) 1 else -1
        val absVal = kotlin.math.abs(motorValue)
        val mapped = deadband + (absVal / 255f) * (255 - deadband)
        return (mapped * sign).toInt().coerceIn(-255, 255)
    }

    private fun calculateAndSend() {
        val model = activeModel.value
        val inputs = model.inputs
        val mixer = model.mixer
        val outputs = model.outputs.normalized()

        // 1. Rescaled Deadzone on Sticks (Starts smoothly from minimum 0% right after threshold)
        val rawT = applyStickDeadzone(tractionValue.value, inputs.tractionDeadzone)
        val rawS = applyStickDeadzone(steeringValue.value, inputs.steeringDeadzone)

        // 2. Apply Expo (Supports negative and positive exponential curves)
        val tExpo = calculateExpo(rawT, inputs.tractionExpo)
        val sExpo = calculateExpo(rawS, inputs.steeringExpo)

        // Screen Quick Trims (-0.2f .. +0.2f normalized)
        val trimT = (inputs.tractionTrim / 50f) * 0.2f
        val trimS = (inputs.steeringTrim / 50f) * 0.2f

        val inT = (tExpo + trimT).coerceIn(-1f, 1f)
        val inS = (sExpo + trimS).coerceIn(-1f, 1f)

        var motorA_demand = 0f
        var motorB_demand = 0f
        var servo_deg = 90

        // 3. MIXING STAGE (Translates inputs into actuator demands in -1.0 .. 1.0)
        when (model.vehicleType) {
            DrivingMode.ARCADE -> {
                val weightT = (mixer.arcadeThrottleWeight.coerceIn(0, 100)) / 100f
                val weightS = (mixer.arcadeSteeringWeight.coerceIn(0, 100)) / 100f

                var left = 0f
                var right = 0f

                if (mixer.arcadeMixMode == 1) {
                    // CURVATURA CONSTANTE (Cheesy Drive)
                    val forward = inT * weightT
                    val turnRate = inS * weightS

                    if (kotlin.math.abs(forward) <= 0.08f) {
                        // Quick-Turn: Giro en el sitio cuando está detenido
                        left = turnRate
                        right = -turnRate
                    } else {
                        // Curvatura proporcional en movimiento
                        val turn = kotlin.math.abs(forward) * turnRate
                        left = forward + turn
                        right = forward - turn

                        // Preservar la curvatura exacta sin saturación
                        val maxMag = maxOf(kotlin.math.abs(left), kotlin.math.abs(right))
                        if (maxMag > 1.0f) {
                            left /= maxMag
                            right /= maxMag
                        }
                    }
                } else {
                    // LINEAL CLÁSICO (Suma y resta diferencial)
                    val throttle = inT * weightT
                    val steering = inS * weightS

                    left = (throttle + steering).coerceIn(-1f, 1f)
                    right = (throttle - steering).coerceIn(-1f, 1f)
                }

                if (mixer.swapAB) {
                    val tmp = left
                    left = right
                    right = tmp
                }

                motorA_demand = left
                motorB_demand = right
                servo_deg = 90
            }

            DrivingMode.TANK -> {
                // Independent dual stick tank: inT = Left Track, inS = Right Track
                if (mixer.swapTracks) {
                    motorA_demand = inS
                    motorB_demand = inT
                } else {
                    motorA_demand = inT
                    motorB_demand = inS
                }
                servo_deg = 90
            }

            DrivingMode.DUAL_DC -> {
                // Motor A = Traction, Motor B = Steering DC Motor
                var t = inT
                var s = inS
                if (mixer.swapAB) {
                    val tmp = t
                    t = s
                    s = tmp
                }
                motorA_demand = t
                motorB_demand = s
                servo_deg = 90
            }

            DrivingMode.SERVO_CAR -> {
                if (mixer.servoMotorOutput == "B") {
                    motorA_demand = 0f
                    motorB_demand = inT
                } else {
                    motorA_demand = inT
                    motorB_demand = 0f
                }

                val steerInput = if (outputs.invertServo) -inS else inS
                val baseAngle = 90 + outputs.trimSteering
                val finalAngle = if (steerInput >= 0f) {
                    val maxRight = 90f * (outputs.epaServoRight / 100f)
                    baseAngle + (steerInput * maxRight)
                } else {
                    val maxLeft = 90f * (outputs.epaServoLeft / 100f)
                    baseAngle + (steerInput * maxLeft)
                }
                servo_deg = finalAngle.toInt().coerceIn(0, 180)
            }
        }

        // 4. OUTPUTS STAGE (Physical Inversion, Band RangeSlider End Points, Deadband on Motor A & Motor B)
        // MOTOR A:
        val dirA = if (outputs.invertMotorA) -motorA_demand else motorA_demand
        val epaA = mapDemandToEpa(dirA, outputs.minEpaMotorA, outputs.maxEpaMotorA)
        val finalA = applyDeadband(epaA.coerceIn(-255, 255), outputs.deadbandA)

        // MOTOR B:
        val dirB = if (outputs.invertMotorB) -motorB_demand else motorB_demand
        val epaB = mapDemandToEpa(dirB, outputs.minEpaMotorB, outputs.maxEpaMotorB)
        val finalB = applyDeadband(epaB.coerceIn(-255, 255), outputs.deadbandB)

        outputMotorA.value = finalA
        outputMotorB.value = finalB
        outputServo.value = servo_deg

        val isConnected = (connectionStatus.value == ConnectionStatus.CONNECTED)
        val inTestMode = _isTestMode.value

        if (isConnected && !inTestMode) {
            sendPayloadImmediate(finalA, finalB, servo_deg, outputs.tractionHardwareMode)
        } else {
            val tag = if (inTestMode) "TEST" else "SIM"
            _lastSentMessage.value = "$tag: A,$finalA,B,$finalB,S,$servo_deg,M,${outputs.tractionHardwareMode}\\n"
        }
    }

    private fun sendPayloadImmediate(a: Int, b: Int, s: Int, m: Int) {
        val payload = "A,$a,B,$b,S,$s,M,$m\n"
        val success = bluetoothService.write(payload)
        if (success) {
            _lastSentMessage.value = "TX: A,$a,B,$b,S,$s,M,$m\\n"
        } else {
            _lastSentMessage.value = "ERR: A,$a,B,$b,S,$s,M,$m\\n"
        }
    }

    // --- LIVE CONTROLLER INPUTS ---
    fun updateTraction(value: Float) {
        tractionValue.value = value.coerceIn(-1f, 1f)
    }

    fun updateSteering(value: Float) {
        steeringValue.value = value.coerceIn(-1f, 1f)
    }

    // --- QUICK TRIMS ---
    fun setTractionTrim(trim: Int) {
        if (!_tractionTrimLocked.value) {
            val clamped = trim.coerceIn(-50, 50)
            updateInputs { it.copy(tractionTrim = clamped) }
        }
    }

    fun toggleTractionTrimLock() {
        _tractionTrimLocked.value = !_tractionTrimLocked.value
    }

    fun setSteeringTrim(trim: Int) {
        if (!_steeringTrimLocked.value) {
            val clamped = trim.coerceIn(-50, 50)
            updateInputs { it.copy(steeringTrim = clamped) }
        }
    }

    fun toggleSteeringTrimLock() {
        _steeringTrimLocked.value = !_steeringTrimLocked.value
    }

    // --- MODEL CONFIGURATION SETTERS ---
    fun updateInputs(transform: (InputsConfig) -> InputsConfig) {
        modelRepository.updateActiveModel { current ->
            current.copy(inputs = transform(current.inputs))
        }
    }

    fun updateMixer(transform: (MixerConfig) -> MixerConfig) {
        modelRepository.updateActiveModel { current ->
            current.copy(mixer = transform(current.mixer))
        }
    }

    fun updateOutputs(transform: (OutputsConfig) -> OutputsConfig) {
        modelRepository.updateActiveModel { current ->
            current.copy(outputs = transform(current.outputs))
        }
    }

    fun setVehicleType(mode: DrivingMode) {
        modelRepository.updateActiveModel { current ->
            current.copy(vehicleType = mode)
        }
        tractionValue.value = 0f
        steeringValue.value = 0f
    }

    fun setModelName(name: String) {
        modelRepository.updateActiveModel { current ->
            current.copy(name = name)
        }
    }

    // --- MODEL MANAGEMENT ---
    fun selectModel(id: String) {
        modelRepository.selectModel(id)
        tractionValue.value = 0f
        steeringValue.value = 0f
    }

    fun createNewModel(name: String, vehicleType: DrivingMode) {
        modelRepository.createModel(name, vehicleType)
    }

    fun duplicateActiveModel() {
        val dup = modelRepository.duplicateModel(activeModel.value.id)
        if (dup != null) {
            modelRepository.selectModel(dup.id)
        }
    }

    fun deleteActiveModel() {
        modelRepository.deleteModel(activeModel.value.id)
    }

    fun linkCurrentConnectedDevice() {
        val dev = connectedDevice.value ?: return
        modelRepository.linkDeviceToActiveModel(dev.address, dev.name)
    }

    fun unlinkDeviceFromActiveModel() {
        modelRepository.unlinkDeviceFromActiveModel()
    }

    fun toggleAutoLoadOnConnect() {
        modelRepository.updateActiveModel { current ->
            current.copy(autoLoadOnConnect = !current.autoLoadOnConnect)
        }
    }

    // --- GAMEPAD SUPPORT ---
    fun handleGamepadJoystick(leftX: Float, leftY: Float, rightX: Float, rightY: Float) {
        val deadzone = 0.15f
        val processedLeftY = if (kotlin.math.abs(leftY) > deadzone) -leftY else 0f
        val processedLeftX = if (kotlin.math.abs(leftX) > deadzone) leftX else 0f
        val processedRightY = if (kotlin.math.abs(rightY) > deadzone) -rightY else 0f
        val processedRightX = if (kotlin.math.abs(rightX) > deadzone) rightX else 0f

        val finalLeftY = processedLeftY.coerceIn(-1f, 1f)
        val finalLeftX = processedLeftX.coerceIn(-1f, 1f)
        val finalRightY = processedRightY.coerceIn(-1f, 1f)
        val finalRightX = processedRightX.coerceIn(-1f, 1f)

        val inputs = activeModel.value.inputs
        if (inputs.unifiedJoystick) {
            if (inputs.swapJoysticks) {
                updateSteering(finalRightX)
                updateTraction(finalRightY)
            } else {
                updateSteering(finalLeftX)
                updateTraction(finalLeftY)
            }
        } else {
            if (inputs.swapJoysticks) {
                updateSteering(finalLeftX)
                updateTraction(finalRightY)
            } else {
                updateTraction(finalLeftY)
                updateSteering(finalRightX)
            }
        }
    }

    fun handleGamepadButtonDown(keyCode: Int) {
        when (keyCode) {
            android.view.KeyEvent.KEYCODE_BUTTON_R1 -> {
                val currentMode = activeModel.value.vehicleType
                val newMode = DrivingMode.values()[(currentMode.ordinal + 1) % DrivingMode.values().size]
                setVehicleType(newMode)
            }
            android.view.KeyEvent.KEYCODE_BUTTON_L1 -> {
                val currentMode = activeModel.value.vehicleType
                var newIndex = currentMode.ordinal - 1
                if (newIndex < 0) newIndex = DrivingMode.values().size - 1
                setVehicleType(DrivingMode.values()[newIndex])
            }
            android.view.KeyEvent.KEYCODE_BUTTON_Y,
            android.view.KeyEvent.KEYCODE_BUTTON_X,
            100 -> {
                val current = activeModel.value.outputs.tractionHardwareMode
                updateOutputs { it.copy(tractionHardwareMode = if (current == 0) 1 else 0) }
            }
        }
    }

    // --- BLUETOOTH METHODS ---
    fun scanDevices() {
        bluetoothService.refreshPairedDevices()
        bluetoothService.startScanning()
    }

    fun stopScanning() {
        bluetoothService.stopScanning()
    }

    fun connectDevice(address: String) {
        bluetoothService.connectToDevice(address)
    }

    fun disconnectDevice() {
        bluetoothService.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        transmissionJob?.cancel()
        connectionObserverJob?.cancel()
        bluetoothService.stopScanning()
        bluetoothService.disconnect()
    }
}
