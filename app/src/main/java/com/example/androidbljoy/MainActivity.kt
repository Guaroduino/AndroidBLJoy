package com.example.androidbljoy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.example.androidbljoy.data.BluetoothService
import com.example.androidbljoy.data.repository.ModelRepository
import com.example.androidbljoy.theme.AndroidBLJoyTheme
import com.example.androidbljoy.ui.main.MainScreenViewModel

enum class AppTheme {
  CYBERPUNK,
  RETRO_AMBER
}

class MainActivity : ComponentActivity() {
  private lateinit var bluetoothService: BluetoothService
  private val viewModel: MainScreenViewModel by viewModels {
    object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainScreenViewModel(
            BluetoothService.getInstance(this@MainActivity),
            ModelRepository.getInstance(this@MainActivity)
        ) as T
      }
    }
  }

  companion object {
    val appThemeState = androidx.compose.runtime.mutableStateOf(AppTheme.CYBERPUNK)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    bluetoothService = BluetoothService.getInstance(this)

    enableEdgeToEdge()
    setContent {
      AndroidBLJoyTheme(appTheme = appThemeState.value) { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation(viewModel) } }
    }
  }

  override fun onPause() {
    super.onPause()
    sendEmergencyStopAndDisconnect()
  }

  override fun onStop() {
    super.onStop()
    sendEmergencyStopAndDisconnect()
  }

  private fun sendEmergencyStopAndDisconnect() {
    bluetoothService.write("V,0,H,0\n")
    bluetoothService.disconnect()
  }

  override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
    if ((ev.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
        ev.action == MotionEvent.ACTION_MOVE) {
        
        val leftX = ev.getAxisValue(MotionEvent.AXIS_X)
        val leftY = ev.getAxisValue(MotionEvent.AXIS_Y)
        val rightX = ev.getAxisValue(MotionEvent.AXIS_Z)
        val rightY = ev.getAxisValue(MotionEvent.AXIS_RZ)
        
        viewModel.handleGamepadJoystick(leftX, leftY, rightX, rightY)
        return true
    }
    return super.dispatchGenericMotionEvent(ev)
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if ((event.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
        (event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
             viewModel.handleGamepadButtonDown(event.keyCode)
             return true
        }
    }
    return super.dispatchKeyEvent(event)
  }
}
