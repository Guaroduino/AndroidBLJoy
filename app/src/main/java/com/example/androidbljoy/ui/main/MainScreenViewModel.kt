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

    // Telemetry feedback of last message
    private val _lastSentMessage = MutableStateFlow("A,0,B,0,S,90,M,0\\n")
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
                if (connectionStatus.value == ConnectionStatus.CONNECTED) {
                    calculateAndSend()
                }
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

    private fun applyExpo(input: Float, expoPercent: Int): Float {
        val expo = expoPercent / 100f
        return (1f - expo) * input + expo * (input * input * input)
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
        val outputs = model.outputs

        // 1. Deadzone on Sticks
        var rawT = tractionValue.value
        if (kotlin.math.abs(rawT) < inputs.tractionDeadzone) rawT = 0f

        var rawS = steeringValue.value
        if (kotlin.math.abs(rawS) < inputs.steeringDeadzone) rawS = 0f

        // 2. Apply Expo
        val tExpo = applyExpo(rawT, inputs.tractionExpo)
        val sExpo = applyExpo(rawS, inputs.steeringExpo)

        // 3. Apply EPA and Trim
        val joyY = ((tExpo * outputs.tractionLimit) + inputs.tractionTrim)
            .toInt().coerceIn(-outputs.tractionLimit, outputs.tractionLimit)
        val joyX = ((sExpo * outputs.steeringLimit) + inputs.steeringTrim)
            .toInt().coerceIn(-outputs.steeringLimit, outputs.steeringLimit)

        var valA = 0
        var valB = 0
        var valS = 90

        // 4. Mixing Stage
        when (model.vehicleType) {
            DrivingMode.DUAL_DC -> {
                val t = if (outputs.invertTraction) -joyY else joyY
                val s = if (outputs.invertSteering) -joyX else joyX
                if (mixer.swapAB) {
                    valA = s
                    valB = t
                } else {
                    valA = t
                    valB = s
                }
                valS = 90
            }

            DrivingMode.TANK -> {
                val left = if (outputs.invertLeftTrack) -joyY else joyY
                val right = if (outputs.invertRightTrack) -joyX else joyX
                if (mixer.swapTracks) {
                    valA = right
                    valB = left
                } else {
                    valA = left
                    valB = right
                }
                valS = 90
            }

            DrivingMode.SERVO_CAR -> {
                val t = if (outputs.invertTraction) -joyY else joyY
                if (mixer.servoMotorOutput == "A") {
                    valA = t
                    valB = 0
                } else {
                    valA = 0
                    valB = t
                }

                val steerInput = if (outputs.invertServo) -sExpo else sExpo
                val baseAngle = 90 + outputs.trimSteering
                val maxDisplacement = 90f * (outputs.epaSteering / 100f)
                val finalAngle = baseAngle + (steerInput * maxDisplacement)
                valS = finalAngle.toInt().coerceIn(0, 180)
            }

            DrivingMode.ARCADE -> {
                val throttle = if (outputs.invertTraction) -joyY else joyY
                val steering = if (outputs.invertSteering) -joyX else joyX

                val left = throttle + steering
                val right = throttle - steering

                if (mixer.swapAB) {
                    valA = right
                    valB = left
                } else {
                    valA = left
                    valB = right
                }
                valS = 90
            }
        }

        // Clamp pre-deadband outputs
        valA = valA.coerceIn(-255, 255)
        valB = valB.coerceIn(-255, 255)

        // 5. Output Deadband
        val finalA = applyDeadband(valA, outputs.deadbandA)
        val finalB = applyDeadband(valB, outputs.deadbandB)

        outputMotorA.value = finalA
        outputMotorB.value = finalB
        outputServo.value = valS

        sendPayloadImmediate(finalA, finalB, valS, outputs.tractionHardwareMode)
    }

    private fun sendPayloadImmediate(a: Int, b: Int, s: Int, m: Int) {
        val payload = "A,$a,B,$b,S,$s,M,$m\n"
        val success = bluetoothService.write(payload)
        if (success) {
            _lastSentMessage.value = "A,$a,B,$b,S,$s,M,$m\\n"
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
