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
        assertEquals(255, model.outputs.tractionLimit)
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
        assertEquals(255, outputs.tractionLimit)
        assertEquals(255, outputs.steeringLimit)
        assertEquals(100, outputs.epaSteering)
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
    }
}
