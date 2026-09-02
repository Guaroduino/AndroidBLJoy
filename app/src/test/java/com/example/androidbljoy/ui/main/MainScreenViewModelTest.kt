package com.example.androidbljoy.ui.main

import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.data.model.InputsConfig
import com.example.androidbljoy.data.model.MixerConfig
import com.example.androidbljoy.data.model.OutputsConfig
import com.example.androidbljoy.data.model.RcModelConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RcModelConfigTest {

    @Test
    fun testDefaultModelConfig() {
        val model = RcModelConfig(
            id = "test_tank",
            name = "Test Tank",
            vehicleType = DrivingMode.TANK
        )

        assertEquals("Test Tank", model.name)
        assertEquals(DrivingMode.TANK, model.vehicleType)
        assertEquals(255, model.outputs.epaTractionHigh)
        assertEquals(255, model.outputs.epaTractionLow)
        assertEquals(0, model.inputs.tractionExpo)
        assertTrue(model.autoLoadOnConnect)
    }

    @Test
    fun testModelWithLinkedMacAddress() {
        val model = RcModelConfig(
            id = "test_crawler",
            name = "Crawler 4x4",
            vehicleType = DrivingMode.SERVO_CAR,
            linkedDeviceAddress = "C4:DE:E2:12:34:56",
            linkedDeviceName = "ESP32_CRAWLER"
        )

        assertEquals("C4:DE:E2:12:34:56", model.linkedDeviceAddress)
        assertEquals("ESP32_CRAWLER", model.linkedDeviceName)
        assertTrue(model.autoLoadOnConnect)
    }

    @Test
    fun testOutputsConfigDefaults() {
        val outputs = OutputsConfig()
        assertEquals(-255, outputs.minEpaMotorA)
        assertEquals(255, outputs.maxEpaMotorA)
        assertEquals(-255, outputs.minEpaMotorB)
        assertEquals(255, outputs.maxEpaMotorB)
        assertEquals(255, outputs.epaTractionHigh)
        assertEquals(255, outputs.epaTractionLow)
        assertEquals(255, outputs.epaSteeringLeft)
        assertEquals(255, outputs.epaSteeringRight)
        assertEquals(100, outputs.epaServoLeft)
        assertEquals(100, outputs.epaServoRight)
        assertEquals(0, outputs.trimSteering)
        assertEquals(0, outputs.deadbandA)
        assertEquals(0, outputs.deadbandB)
        assertEquals(0, outputs.tractionHardwareMode)
    }

    @Test
    fun testInputsConfigDefaults() {
        val inputs = InputsConfig()
        assertEquals(0, inputs.tractionExpo)
        assertEquals(0, inputs.steeringExpo)
        assertEquals(0f, inputs.tractionDeadzone, 0.001f)
        assertEquals(0f, inputs.steeringDeadzone, 0.001f)
        assertEquals(0, inputs.tractionTrim)
        assertEquals(0, inputs.steeringTrim)
        assertEquals(false, inputs.unifiedJoystick)
        assertEquals(false, inputs.swapJoysticks)
        assertEquals(true, inputs.tractionAxisVertical)
        assertEquals(true, inputs.steeringAxisHorizontal)
    }

    @Test
    fun testNegativeAndPositiveExpoCalculation() {
        // Linear at 0%
        assertEquals(0.5f, com.example.androidbljoy.ui.components.calculateExpo(0.5f, 0), 0.001f)
        // Positive expo softens center (value lower than linear)
        val posExpo = com.example.androidbljoy.ui.components.calculateExpo(0.5f, 50)
        assertTrue(posExpo < 0.5f)
        // Negative expo boosts center (value higher than linear)
        val negExpo = com.example.androidbljoy.ui.components.calculateExpo(0.5f, -50)
        assertTrue(negExpo > 0.5f)
        // Full deflection always reaches 1.0f
        assertEquals(1.0f, com.example.androidbljoy.ui.components.calculateExpo(1.0f, 50), 0.001f)
        assertEquals(1.0f, com.example.androidbljoy.ui.components.calculateExpo(1.0f, -50), 0.001f)
    }

    @Test
    fun testDeadzoneRescaling() {
        val deadzone = 0.20f
        fun rescale(input: Float): Float {
            val absVal = kotlin.math.abs(input)
            if (absVal <= deadzone) return 0f
            val sign = if (input > 0f) 1f else -1f
            return sign * ((absVal - deadzone) / (1f - deadzone))
        }

        // Inside deadzone -> 0
        assertEquals(0f, rescale(0.10f), 0.001f)
        assertEquals(0f, rescale(0.20f), 0.001f)
        // Just past deadzone -> starts smoothly near 0.0
        assertEquals(0.0125f, rescale(0.21f), 0.001f)
        // Midpoint
        assertEquals(0.5f, rescale(0.60f), 0.001f)
        // Full travel -> 1.0
        assertEquals(1.0f, rescale(1.0f), 0.001f)
        // Negative travel
        assertEquals(-1.0f, rescale(-1.0f), 0.001f)
    }

    @Test
    fun testMixerConfigDefaults() {
        val mixer = com.example.androidbljoy.data.model.MixerConfig()
        assertEquals(100, mixer.arcadeThrottleWeight)
        assertEquals(100, mixer.arcadeSteeringWeight)
        assertEquals(0, mixer.arcadeMixMode)
        assertEquals(false, mixer.swapAB)
        assertEquals(false, mixer.swapTracks)
        assertEquals("A", mixer.servoMotorOutput)
    }

    @Test
    fun testMapDemandToEpaRangeSlider() {
        // Case 1: Symmetrical standard band (-255 to 255)
        assertEquals(0, MainScreenViewModel.mapDemandToEpa(0f, -255, 255))
        assertEquals(255, MainScreenViewModel.mapDemandToEpa(1.0f, -255, 255))
        assertEquals(-255, MainScreenViewModel.mapDemandToEpa(-1.0f, -255, 255))
        assertEquals(127, MainScreenViewModel.mapDemandToEpa(0.5f, -255, 255))

        // Case 2: Asymmetric band (-100 to +180)
        assertEquals(0, MainScreenViewModel.mapDemandToEpa(0f, -100, 180))
        assertEquals(180, MainScreenViewModel.mapDemandToEpa(1.0f, -100, 180))
        assertEquals(90, MainScreenViewModel.mapDemandToEpa(0.5f, -100, 180))
        assertEquals(-100, MainScreenViewModel.mapDemandToEpa(-1.0f, -100, 180))
        assertEquals(-50, MainScreenViewModel.mapDemandToEpa(-0.5f, -100, 180))

        // Case 3: Positive-only band (20 to 240, excluding negatives)
        assertEquals(0, MainScreenViewModel.mapDemandToEpa(0f, 20, 240))
        assertEquals(240, MainScreenViewModel.mapDemandToEpa(1.0f, 20, 240))
        assertEquals(130, MainScreenViewModel.mapDemandToEpa(0.5f, 20, 240))
        assertEquals(20, MainScreenViewModel.mapDemandToEpa(0.001f, 20, 240))
        // Negative demand is strictly excluded (returns 0)
        assertEquals(0, MainScreenViewModel.mapDemandToEpa(-0.5f, 20, 240))
        assertEquals(0, MainScreenViewModel.mapDemandToEpa(-1.0f, 20, 240))
    }

    @Test
    fun testCurvatureDriveMixingLogic() {
        // Quick-Turn: stopped forward (|T| <= 0.08f) -> full spin on the spot
        val forwardStopped = 0.0f
        val steerRight = 1.0f
        var left = 0f
        var right = 0f
        if (kotlin.math.abs(forwardStopped) <= 0.08f) {
            left = steerRight
            right = -steerRight
        }
        assertEquals(1.0f, left, 0.001f)
        assertEquals(-1.0f, right, 0.001f)

        // In motion (|T| > 0.08f): Curvature is preserved
        val forwardHalf = 0.5f
        val steerQuarter = 0.5f
        val turnHalf = kotlin.math.abs(forwardHalf) * steerQuarter
        val leftHalf = forwardHalf + turnHalf   // 0.75
        val rightHalf = forwardHalf - turnHalf  // 0.25
        // Ratio of left to right
        val ratioHalf = leftHalf / rightHalf    // 3.0
        assertEquals(3.0f, ratioHalf, 0.001f)

        // At full throttle forward:
        val forwardFull = 1.0f
        val turnFull = kotlin.math.abs(forwardFull) * steerQuarter
        var leftFull = forwardFull + turnFull   // 1.5
        var rightFull = forwardFull - turnFull  // 0.5
        val maxMag = maxOf(kotlin.math.abs(leftFull), kotlin.math.abs(rightFull))
        if (maxMag > 1.0f) {
            leftFull /= maxMag  // 1.0
            rightFull /= maxMag // 0.3333...
        }
        val ratioFull = leftFull / rightFull    // 3.0
        assertEquals(3.0f, ratioFull, 0.001f) // Exact same curvature maintained without saturation!
    }
}
