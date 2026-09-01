package com.example.androidbljoy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbljoy.data.BluetoothDeviceInfo
import com.example.androidbljoy.data.BluetoothService
import com.example.androidbljoy.data.ConnectionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DrivingMode {
    DUAL_DC,
    TANK,
    SERVO_CAR,
    ARCADE
}

class MainScreenViewModel(val bluetoothService: BluetoothService) : ViewModel() {

    // Connection and Scanner flows
    val connectionStatus: StateFlow<ConnectionStatus> = bluetoothService.connectionStatus
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = bluetoothService.connectedDevice
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = bluetoothService.pairedDevices
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>> = bluetoothService.scannedDevices
    val isScanning: StateFlow<Boolean> = bluetoothService.isScanning
    val rssi: StateFlow<Int?> = bluetoothService.rssi


    // Mode Selector
    private val _drivingMode = MutableStateFlow(DrivingMode.DUAL_DC)
    val drivingMode: StateFlow<DrivingMode> = _drivingMode.asStateFlow()

    // Mode 1 settings (Dual DC)
    private val _invertTraction = MutableStateFlow(false)
    val invertTraction: StateFlow<Boolean> = _invertTraction.asStateFlow()

    private val _invertSteering = MutableStateFlow(false)
    val invertSteering: StateFlow<Boolean> = _invertSteering.asStateFlow()

    private val _swapAB = MutableStateFlow(false)
    val swapAB: StateFlow<Boolean> = _swapAB.asStateFlow()

    // Mode 2 settings (Tank)
    private val _invertLeftTrack = MutableStateFlow(false)
    val invertLeftTrack: StateFlow<Boolean> = _invertLeftTrack.asStateFlow()

    private val _invertRightTrack = MutableStateFlow(false)
    val invertRightTrack: StateFlow<Boolean> = _invertRightTrack.asStateFlow()

    private val _swapTracks = MutableStateFlow(false)
    val swapTracks: StateFlow<Boolean> = _swapTracks.asStateFlow()

    // Mode 3 settings (Servo Car)
    private val _servoMotorOutput = MutableStateFlow("A") // "A" or "B"
    val servoMotorOutput: StateFlow<String> = _servoMotorOutput.asStateFlow()

    private val _invertTractionServo = MutableStateFlow(false)
    val invertTractionServo: StateFlow<Boolean> = _invertTractionServo.asStateFlow()

    private val _invertServo = MutableStateFlow(false)
    val invertServo: StateFlow<Boolean> = _invertServo.asStateFlow()

    private val _trimSteering = MutableStateFlow(0) // -45 to +45
    val trimSteering: StateFlow<Int> = _trimSteering.asStateFlow()

    private val _epaSteering = MutableStateFlow(100) // 10% to 100%
    val epaSteering: StateFlow<Int> = _epaSteering.asStateFlow()

    // Universal Limits (EPA)
    private val _tractionLimit = MutableStateFlow(255)
    val tractionLimit: StateFlow<Int> = _tractionLimit.asStateFlow()

    private val _steeringLimit = MutableStateFlow(255)
    val steeringLimit: StateFlow<Int> = _steeringLimit.asStateFlow()

    // Expo
    private val _tractionExpo = MutableStateFlow(0) // 0 to 100%
    val tractionExpo: StateFlow<Int> = _tractionExpo.asStateFlow()

    private val _steeringExpo = MutableStateFlow(0) // 0 to 100%
    val steeringExpo: StateFlow<Int> = _steeringExpo.asStateFlow()

    // Deadband
    private val _deadbandA = MutableStateFlow(0) // 0 to 127
    val deadbandA: StateFlow<Int> = _deadbandA.asStateFlow()

    private val _deadbandB = MutableStateFlow(0) // 0 to 127
    val deadbandB: StateFlow<Int> = _deadbandB.asStateFlow()

    private val _transmissionDelayMs = MutableStateFlow(20L)
    val transmissionDelayMs: StateFlow<Long> = _transmissionDelayMs.asStateFlow()

    // Live controller values (normalized -1.0f to 1.0f)
    val tractionValue = MutableStateFlow(0f)
    val steeringValue = MutableStateFlow(0f)

    // Trim and Lock settings
    private val _tractionTrim = MutableStateFlow(0) // -50 to 50
    val tractionTrim: StateFlow<Int> = _tractionTrim.asStateFlow()

    private val _tractionTrimLocked = MutableStateFlow(false)
    val tractionTrimLocked: StateFlow<Boolean> = _tractionTrimLocked.asStateFlow()

    private val _steeringTrim = MutableStateFlow(0) // -50 to 50
    val steeringTrim: StateFlow<Int> = _steeringTrim.asStateFlow()

    private val _steeringTrimLocked = MutableStateFlow(false)
    val steeringTrimLocked: StateFlow<Boolean> = _steeringTrimLocked.asStateFlow()


    // UI feedback flow for last sent message
    private val _lastSentMessage = MutableStateFlow("A,0,B,0,S,90\\n")
    val lastSentMessage: StateFlow<String> = _lastSentMessage.asStateFlow()

    private val _swapJoysticks = MutableStateFlow(false)
    val swapJoysticks: StateFlow<Boolean> = _swapJoysticks.asStateFlow()

    private val _unifiedJoystick = MutableStateFlow(false)
    val unifiedJoystick: StateFlow<Boolean> = _unifiedJoystick.asStateFlow()

    private val _tractionHardwareMode = MutableStateFlow(0) // 0 = DC, 1 = ESC
    val tractionHardwareMode: StateFlow<Int> = _tractionHardwareMode.asStateFlow()

    private var transmissionJob: Job? = null

    init {
        // Start transmission loop
        startTransmissionLoop()
    }

    private fun startTransmissionLoop() {
        transmissionJob?.cancel()
        transmissionJob = viewModelScope.launch {
            while (true) {
                if (connectionStatus.value == ConnectionStatus.CONNECTED) {
                    calculateAndSend()
                }
                delay(20L) // Global Keep-Alive loop interval (20ms for 50Hz refresh rate)
            }
        }
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
        // 1. Appply Expo
        val tExpo = applyExpo(tractionValue.value, _tractionExpo.value)
        val sExpo = applyExpo(steeringValue.value, _steeringExpo.value)

        // 2. Apply EPA and Trim
        val joyY = ((tExpo * _tractionLimit.value) + _tractionTrim.value).toInt().coerceIn(-_tractionLimit.value, _tractionLimit.value)
        val joyX = ((sExpo * _steeringLimit.value) + _steeringTrim.value).toInt().coerceIn(-_steeringLimit.value, _steeringLimit.value)

        var valA = 0
        var valB = 0
        var valS = 90

        // 3. Mixing
        when (_drivingMode.value) {
            DrivingMode.DUAL_DC -> {
                val t = if (_invertTraction.value) -joyY else joyY
                val s = if (_invertSteering.value) -joyX else joyX
                if (_swapAB.value) {
                    valA = s
                    valB = t
                } else {
                    valA = t
                    valB = s
                }
                valS = 90
            }
            DrivingMode.TANK -> {
                val left = if (_invertLeftTrack.value) -joyY else joyY
                val right = if (_invertRightTrack.value) -joyX else joyX
                if (_swapTracks.value) {
                    valA = right
                    valB = left
                } else {
                    valA = left
                    valB = right
                }
                valS = 90
            }
            DrivingMode.SERVO_CAR -> {
                val t = if (_invertTractionServo.value) -joyY else joyY
                if (_servoMotorOutput.value == "A") {
                    valA = t
                    valB = 0
                } else {
                    valA = 0
                    valB = t
                }

                // Servo steering handles EPA separately, but uses sExpo
                val steerInput = if (_invertServo.value) -sExpo else sExpo
                val baseAngle = 90 + _trimSteering.value
                val maxDisplacement = 90f * (_epaSteering.value / 100f)
                val finalAngle = baseAngle + (steerInput * maxDisplacement)
                valS = finalAngle.toInt().coerceIn(0, 180)
            }
            DrivingMode.ARCADE -> {
                val throttle = if (_invertTraction.value) -joyY else joyY
                val steering = if (_invertSteering.value) -joyX else joyX
                
                val left = throttle + steering
                val right = throttle - steering
                
                if (_swapAB.value) {
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

        // 4. Apply Deadband
        val finalA = applyDeadband(valA, _deadbandA.value)
        val finalB = applyDeadband(valB, _deadbandB.value)

        sendPayloadImmediate(finalA, finalB, valS)
    }

    private fun sendPayloadImmediate(a: Int, b: Int, s: Int) {
        val m = _tractionHardwareMode.value
        val payload = "A,$a,B,$b,S,$s,M,$m\n"
        val success = bluetoothService.write(payload)
        if (success) {
            _lastSentMessage.value = "A,$a,B,$b,S,$s,M,$m\\n"
        }
    }

    fun setDrivingMode(mode: DrivingMode) {
        _drivingMode.value = mode
        // Reset joystick inputs on mode change to prevent unexpected movements
        tractionValue.value = 0f
        steeringValue.value = 0f
    }

    // Setters for Mode 1
    fun setInvertTraction(enabled: Boolean) {
        _invertTraction.value = enabled
    }

    fun setInvertSteering(enabled: Boolean) {
        _invertSteering.value = enabled
    }

    fun setSwapAB(enabled: Boolean) {
        _swapAB.value = enabled
    }

    // Setters for Mode 2
    fun setInvertLeftTrack(enabled: Boolean) {
        _invertLeftTrack.value = enabled
    }

    fun setInvertRightTrack(enabled: Boolean) {
        _invertRightTrack.value = enabled
    }

    fun setSwapTracks(enabled: Boolean) {
        _swapTracks.value = enabled
    }

    // Setters for Mode 3
    fun setServoMotorOutput(output: String) {
        _servoMotorOutput.value = output
    }

    fun setInvertTractionServo(enabled: Boolean) {
        _invertTractionServo.value = enabled
    }

    fun setInvertServo(enabled: Boolean) {
        _invertServo.value = enabled
    }

    fun setTrimSteering(trim: Int) {
        _trimSteering.value = trim.coerceIn(-45, 45)
    }

    fun setEpaSteering(epa: Int) {
        _epaSteering.value = epa.coerceIn(10, 100)
    }

    // Universal limits
    fun setTractionLimit(limit: Int) {
        _tractionLimit.value = limit.coerceIn(0, 255)
    }

    fun setSteeringLimit(limit: Int) {
        _steeringLimit.value = limit.coerceIn(0, 255)
    }

    fun setTractionExpo(expo: Int) {
        _tractionExpo.value = expo.coerceIn(0, 100)
    }

    fun setSteeringExpo(expo: Int) {
        _steeringExpo.value = expo.coerceIn(0, 100)
    }

    fun setDeadbandA(db: Int) {
        _deadbandA.value = db.coerceIn(0, 127)
    }

    fun setDeadbandB(db: Int) {
        _deadbandB.value = db.coerceIn(0, 127)
    }

    fun updateTraction(value: Float) {
        tractionValue.value = value.coerceIn(-1f, 1f)
    }

    fun updateSteering(value: Float) {
        steeringValue.value = value.coerceIn(-1f, 1f)
    }

    // Trim and Lock settings
    fun setTractionTrim(trim: Int) {
        if (!_tractionTrimLocked.value) {
            _tractionTrim.value = trim.coerceIn(-50, 50)
        }
    }

    fun toggleTractionTrimLock() {
        _tractionTrimLocked.value = !_tractionTrimLocked.value
    }

    fun setSteeringTrim(trim: Int) {
        if (!_steeringTrimLocked.value) {
            _steeringTrim.value = trim.coerceIn(-50, 50)
        }
    }

    fun toggleSteeringTrimLock() {
        _steeringTrimLocked.value = !_steeringTrimLocked.value
    }

    fun toggleSwapJoysticks() {
        _swapJoysticks.value = !_swapJoysticks.value
    }

    fun toggleUnifiedJoystick() {
        _unifiedJoystick.value = !_unifiedJoystick.value
    }

    fun setTractionHardwareMode(mode: Int) {
        _tractionHardwareMode.value = mode
    }

    // --- GAMEPAD SUPPORT ---
    fun handleGamepadJoystick(leftX: Float, leftY: Float, rightX: Float, rightY: Float) {
        val deadzone = 0.15f

        // X/Y axes are usually -1.0 to 1.0. For Y, -1.0 is UP, 1.0 is DOWN.
        // We invert Y so UP is positive (1.0f) for traction.
        val processedLeftY = if (kotlin.math.abs(leftY) > deadzone) -leftY else 0f
        val processedLeftX = if (kotlin.math.abs(leftX) > deadzone) leftX else 0f
        val processedRightY = if (kotlin.math.abs(rightY) > deadzone) -rightY else 0f
        val processedRightX = if (kotlin.math.abs(rightX) > deadzone) rightX else 0f

        val finalLeftY = processedLeftY.coerceIn(-1f, 1f)
        val finalLeftX = processedLeftX.coerceIn(-1f, 1f)
        val finalRightY = processedRightY.coerceIn(-1f, 1f)
        val finalRightX = processedRightX.coerceIn(-1f, 1f)

        // Based on app mapping: Left Stick = Traction (Y), Right Stick = Steering (X)
        if (_unifiedJoystick.value) {
            // Unified mode enabled
            if (_swapJoysticks.value) {
                // Unified Right: Ignore Left Stick
                updateSteering(finalRightX)
                updateTraction(finalRightY)
            } else {
                // Unified Left: Ignore Right Stick
                updateSteering(finalLeftX)
                updateTraction(finalLeftY)
            }
        } else {
            // Separated mode
            if (_swapJoysticks.value) {
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
                // Cycle Driving Mode Forward
                val currentMode = _drivingMode.value
                val newMode = DrivingMode.values()[(currentMode.ordinal + 1) % DrivingMode.values().size]
                setDrivingMode(newMode)
            }
            android.view.KeyEvent.KEYCODE_BUTTON_L1 -> {
                // Cycle Driving Mode Backward
                val currentMode = _drivingMode.value
                var newIndex = currentMode.ordinal - 1
                if (newIndex < 0) newIndex = DrivingMode.values().size - 1
                setDrivingMode(DrivingMode.values()[newIndex])
            }
            android.view.KeyEvent.KEYCODE_BUTTON_Y,
            android.view.KeyEvent.KEYCODE_BUTTON_X, // Fallback for Triangle/Y
            100 -> { // Triangle
                // Toggle Traction Hardware Mode
                val current = _tractionHardwareMode.value
                setTractionHardwareMode(if (current == 0) 1 else 0)
            }
        }
    }
    // -----------------------

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
        bluetoothService.stopScanning()
        bluetoothService.disconnect()
    }
}
