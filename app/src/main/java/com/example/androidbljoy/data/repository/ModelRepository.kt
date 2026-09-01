package com.example.androidbljoy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.androidbljoy.data.model.DrivingMode
import com.example.androidbljoy.data.model.RcModelConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ModelRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("rc_models_preferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _models = MutableStateFlow<List<RcModelConfig>>(emptyList())
    val models: StateFlow<List<RcModelConfig>> = _models.asStateFlow()

    private val _activeModel = MutableStateFlow(createDefaultModels().first())
    val activeModel: StateFlow<RcModelConfig> = _activeModel.asStateFlow()

    init {
        loadModels()
    }

    companion object {
        @Volatile
        private var INSTANCE: ModelRepository? = null

        fun getInstance(context: Context): ModelRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ModelRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        private fun createDefaultModels(): List<RcModelConfig> {
            return listOf(
                RcModelConfig(
                    id = "default_dual_dc",
                    name = "Doble Motor Clásico",
                    vehicleType = DrivingMode.DUAL_DC
                ),
                RcModelConfig(
                    id = "default_tank",
                    name = "Tanque Oruga",
                    vehicleType = DrivingMode.TANK
                ),
                RcModelConfig(
                    id = "default_servo_car",
                    name = "Coche Servo",
                    vehicleType = DrivingMode.SERVO_CAR
                ),
                RcModelConfig(
                    id = "default_arcade",
                    name = "Rover Arcade",
                    vehicleType = DrivingMode.ARCADE
                )
            )
        }
    }

    private fun loadModels() {
        val json = prefs.getString("saved_models", null)
        val loadedList: List<RcModelConfig> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<RcModelConfig>>() {}.type
                gson.fromJson<List<RcModelConfig>>(json, type) ?: createDefaultModels()
            } catch (e: Exception) {
                e.printStackTrace()
                createDefaultModels()
            }
        } else {
            createDefaultModels()
        }

        _models.value = loadedList

        val savedActiveId = prefs.getString("active_model_id", null)
        val active = loadedList.firstOrNull { it.id == savedActiveId } ?: loadedList.first()
        _activeModel.value = active
    }

    private fun persistModels(list: List<RcModelConfig>) {
        _models.value = list
        prefs.edit().putString("saved_models", gson.toJson(list)).apply()
    }

    fun selectModel(id: String) {
        val model = _models.value.firstOrNull { it.id == id }
        if (model != null) {
            _activeModel.value = model
            prefs.edit().putString("active_model_id", id).apply()
        }
    }

    fun saveModel(model: RcModelConfig) {
        val currentList = _models.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == model.id }
        if (index >= 0) {
            currentList[index] = model
        } else {
            currentList.add(model)
        }
        persistModels(currentList)

        if (_activeModel.value.id == model.id) {
            _activeModel.value = model
        }
    }

    fun updateActiveModel(transform: (RcModelConfig) -> RcModelConfig) {
        val current = _activeModel.value
        val updated = transform(current)
        _activeModel.value = updated
        saveModel(updated)
    }

    fun createModel(name: String, vehicleType: DrivingMode): RcModelConfig {
        val newModel = RcModelConfig(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Modelo ${_models.value.size + 1}" },
            vehicleType = vehicleType
        )
        val currentList = _models.value.toMutableList()
        currentList.add(newModel)
        persistModels(currentList)
        selectModel(newModel.id)
        return newModel
    }

    fun duplicateModel(id: String): RcModelConfig? {
        val source = _models.value.firstOrNull { it.id == id } ?: return null
        val duplicate = source.copy(
            id = UUID.randomUUID().toString(),
            name = "${source.name} (Copia)",
            linkedDeviceAddress = null,
            linkedDeviceName = null
        )
        val currentList = _models.value.toMutableList()
        currentList.add(duplicate)
        persistModels(currentList)
        return duplicate
    }

    fun deleteModel(id: String) {
        if (_models.value.size <= 1) return // Keep at least one model
        val currentList = _models.value.filterNot { it.id == id }
        persistModels(currentList)

        if (_activeModel.value.id == id) {
            selectModel(currentList.first().id)
        }
    }

    /**
     * Model Match: Looks for a model linked to this Bluetooth device MAC.
     */
    fun findModelForBleDevice(address: String): RcModelConfig? {
        return _models.value.firstOrNull {
            it.linkedDeviceAddress.equals(address, ignoreCase = true) && it.autoLoadOnConnect
        }
    }

    fun linkDeviceToActiveModel(macAddress: String, deviceName: String) {
        updateActiveModel { current ->
            current.copy(
                linkedDeviceAddress = macAddress,
                linkedDeviceName = deviceName
            )
        }
    }

    fun unlinkDeviceFromActiveModel() {
        updateActiveModel { current ->
            current.copy(
                linkedDeviceAddress = null,
                linkedDeviceName = null
            )
        }
    }
}
