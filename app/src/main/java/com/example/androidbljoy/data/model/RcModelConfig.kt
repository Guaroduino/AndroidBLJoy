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
    val swapJoysticks: Boolean = false,
    val tractionAxisVertical: Boolean = true,  // true: Eje Vertical (Y), false: Eje Horizontal (X)
    val steeringAxisHorizontal: Boolean = true // true: Eje Horizontal (X), false: Eje Vertical (Y)
)

data class MixerConfig(
    val swapAB: Boolean = false,             // Swap Motor A / Motor B
    val swapTracks: Boolean = false,         // Swap Left/Right Tank tracks
    val servoMotorOutput: String = "A",      // "A" or "B" for SERVO_CAR
    val arcadeThrottleWeight: Int = 100,     // Peso / Sensibilidad de Tracción en Arcade (0..100%)
    val arcadeSteeringWeight: Int = 100,     // Peso / Sensibilidad de Dirección en Arcade (0..100%)
    val arcadeMixMode: Int = 0               // 0 = LINEAL (Clásico), 1 = CURVATURA (Cheesy Drive)
)

data class OutputsConfig(
    val invertMotorA: Boolean = false,       // Invertir Motor A (Tracción o Rueda/Oruga Izq)
    val invertMotorB: Boolean = false,       // Invertir Motor B (Dirección o Rueda/Oruga Der)
    val invertServo: Boolean = false,        // Invertir Servo (Coche Servo)
    val minEpaMotorA: Int = -255,            // Límite Inferior Motor A (-255..255)
    val maxEpaMotorA: Int = 255,             // Límite Superior Motor A (-255..255)
    val minEpaMotorB: Int = -255,            // Límite Inferior Motor B (-255..255)
    val maxEpaMotorB: Int = 255,             // Límite Superior Motor B (-255..255)
    val epaServoLeft: Int = 100,             // EPA Servo Izquierda % (10..100)
    val epaServoRight: Int = 100,            // EPA Servo Derecha % (10..100)
    val trimSteering: Int = 0,               // Subtrim Servo (-45..45°)
    val deadbandA: Int = 0,                  // 0..127
    val deadbandB: Int = 0,                  // 0..127
    val tractionHardwareMode: Int = 0        // 0 = DC Driver, 1 = ESC
) {
    // Compatibility aliases
    val invertTraction: Boolean get() = invertMotorA
    val invertSteering: Boolean get() = invertMotorB
    val invertLeftTrack: Boolean get() = invertMotorA
    val invertRightTrack: Boolean get() = invertMotorB
    val epaMotorAHigh: Int get() = maxEpaMotorA
    val epaMotorALow: Int get() = kotlin.math.abs(minEpaMotorA)
    val epaMotorBHigh: Int get() = maxEpaMotorB
    val epaMotorBLow: Int get() = kotlin.math.abs(minEpaMotorB)
    val epaTractionHigh: Int get() = maxEpaMotorA
    val epaTractionLow: Int get() = kotlin.math.abs(minEpaMotorA)
    val epaSteeringLeft: Int get() = kotlin.math.abs(minEpaMotorB)
    val epaSteeringRight: Int get() = maxEpaMotorB

    fun normalized(): OutputsConfig {
        val minA = minEpaMotorA.coerceIn(-255, 255)
        val maxA = maxEpaMotorA.coerceIn(-255, 255)
        val minB = minEpaMotorB.coerceIn(-255, 255)
        val maxB = maxEpaMotorB.coerceIn(-255, 255)
        return copy(
            minEpaMotorA = if (minA > maxA) maxA else minA,
            maxEpaMotorA = if (maxA < minA) minA else maxA,
            minEpaMotorB = if (minB > maxB) maxB else minB,
            maxEpaMotorB = if (maxB < minB) minB else maxB,
            epaServoLeft = if (epaServoLeft <= 0) 100 else epaServoLeft,
            epaServoRight = if (epaServoRight <= 0) 100 else epaServoRight
        )
    }
}

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
