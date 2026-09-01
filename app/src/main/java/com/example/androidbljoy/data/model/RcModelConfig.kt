package com.example.androidbljoy.data.model

import java.util.UUID

enum class DrivingMode {
    DUAL_DC,
    TANK,
    SERVO_CAR,
    ARCADE
}

data class InputsConfig(
    val tractionExpo: Int = 0,         // 0 to 100%
    val steeringExpo: Int = 0,         // 0 to 100%
    val tractionDeadzone: Float = 0f,  // 0f to 0.3f
    val steeringDeadzone: Float = 0f,  // 0f to 0.3f
    val tractionTrim: Int = 0,         // -50 to 50
    val steeringTrim: Int = 0,         // -50 to 50
    val unifiedJoystick: Boolean = false,
    val swapJoysticks: Boolean = false
)

data class MixerConfig(
    val swapAB: Boolean = false,        // Swap Motor A / Motor B
    val swapTracks: Boolean = false,    // Swap Left/Right Tank tracks
    val servoMotorOutput: String = "A"  // "A" or "B" for SERVO_CAR
)

data class OutputsConfig(
    val invertTraction: Boolean = false,
    val invertSteering: Boolean = false,
    val invertLeftTrack: Boolean = false,
    val invertRightTrack: Boolean = false,
    val invertServo: Boolean = false,
    val tractionLimit: Int = 255,       // EPA Tracción 0..255
    val steeringLimit: Int = 255,       // EPA Dirección 0..255
    val epaSteering: Int = 100,         // EPA Servo % (10..100)
    val trimSteering: Int = 0,          // Subtrim Servo (-45..45°)
    val deadbandA: Int = 0,             // 0..127
    val deadbandB: Int = 0,             // 0..127
    val tractionHardwareMode: Int = 0   // 0 = DC Driver, 1 = ESC
)

data class RcModelConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Nuevo Modelo",
    val vehicleType: DrivingMode = DrivingMode.DUAL_DC,
    val linkedDeviceAddress: String? = null,
    val linkedDeviceName: String? = null,
    val autoLoadOnConnect: Boolean = true,
    val inputs: InputsConfig = InputsConfig(),
    val mixer: MixerConfig = MixerConfig(),
    val outputs: OutputsConfig = OutputsConfig()
)
