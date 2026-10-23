package com.example.androidbljoy

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidbljoy.data.BluetoothService
import com.example.androidbljoy.data.repository.ModelRepository
import com.example.androidbljoy.theme.AndroidBLJoyTheme
import com.example.androidbljoy.ui.main.MainScreenViewModel

enum class AppTheme {
  CYBERPUNK,
  RETRO_AMBER,
  CARBON_LCD
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
    private const val PREFS_NAME = "bljoy_theme_prefs"
    private const val KEY_THEME = "selected_theme"
    val appThemeState = androidx.compose.runtime.mutableStateOf(AppTheme.CARBON_LCD)

    fun setTheme(context: Context, theme: AppTheme) {
      appThemeState.value = theme
      context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putString(KEY_THEME, theme.name)
          .apply()
    }

    fun loadTheme(context: Context) {
      val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .getString(KEY_THEME, AppTheme.CARBON_LCD.name)
      val theme = try {
        AppTheme.valueOf(saved ?: AppTheme.CARBON_LCD.name)
      } catch (_: Exception) {
        AppTheme.CARBON_LCD
      }
      appThemeState.value = theme
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    loadTheme(this)
    bluetoothService = BluetoothService.getInstance(this)

    // Keep screen on while controlling/testing
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    // True immersive fullscreen: hides status bar and navigation bar completely
    hideSystemBars()

    setContent {
      AndroidBLJoyTheme(appTheme = appThemeState.value) { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation(viewModel) } }
    }
  }

  override fun onResume() {
    super.onResume()
    hideSystemBars()
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
      hideSystemBars()
    }
  }

  private fun hideSystemBars() {
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
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
