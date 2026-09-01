package com.example.androidbljoy.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class BluetoothDeviceInfo(
    val name: String,
    val address: String
)

class BluetoothService private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: BluetoothService? = null

        fun getInstance(context: Context): BluetoothService {
            return INSTANCE ?: synchronized(this) {
                val instance = BluetoothService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    private val TAG = "BluetoothService"
    
    // ESP32-C3 Target BLE service and characteristic UUIDs
    private val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = _connectedDevice.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _rssi = MutableStateFlow<Int?>(null)
    val rssi: StateFlow<Int?> = _rssi.asStateFlow()

    private var rssiJob: kotlinx.coroutines.Job? = null

    @SuppressLint("MissingPermission")
    private fun startRssiPolling() {
        rssiJob?.cancel()
        rssiJob = ioScope.launch {
            while (_connectionStatus.value == ConnectionStatus.CONNECTED) {
                try {
                    bluetoothGatt?.readRemoteRssi()
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading RSSI", e)
                }
                kotlinx.coroutines.delay(2000L)
            }
            _rssi.value = null
        }
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var targetCharacteristic: BluetoothGattCharacteristic? = null

    private val ioScope = CoroutineScope(Dispatchers.IO)

    // BLE Scan callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            // Get scan record name or device name, fallback to Unknown
            val name = result.scanRecord?.deviceName ?: device.name ?: "Dispositivo Desconocido"
            val address = device.address
            val info = BluetoothDeviceInfo(name, address)
            if (!_scannedDevices.value.any { d -> d.address == address }) {
                _scannedDevices.value = _scannedDevices.value + info
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed with error code: $errorCode")
            _isScanning.value = false
        }
    }

    // GATT Callback to handle connection, service discovery, and write results
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange status: $status, newState: $newState")

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT connection failed with status: $status. Disconnecting...")
                closeGatt()
                _connectionStatus.value = ConnectionStatus.ERROR
                _connectedDevice.value = null
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "GATT Connected. Starting service discovery...")
                // Discover services - mandatory before we declare the UI as connected
                val success = gatt.discoverServices()
                if (!success) {
                    Log.e(TAG, "Failed to start service discovery")
                    closeGatt()
                    _connectionStatus.value = ConnectionStatus.ERROR
                    _connectedDevice.value = null
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "GATT Disconnected.")
                closeGatt()
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _connectedDevice.value = null
                _rssi.value = null
                rssiJob?.cancel()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "onServicesDiscovered status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    if (characteristic != null) {
                        targetCharacteristic = characteristic
                        val prioritySuccess = gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        Log.d(TAG, "Target BLE Service and Characteristic captured. High priority request success: $prioritySuccess")
                        _connectionStatus.value = ConnectionStatus.CONNECTED
                        startRssiPolling()
                    } else {
                        Log.e(TAG, "Characteristic not found inside service")
                        closeGatt()
                        _connectionStatus.value = ConnectionStatus.ERROR
                        _connectedDevice.value = null
                    }
                } else {
                    Log.e(TAG, "Target BLE Service not found on device")
                    closeGatt()
                    _connectionStatus.value = ConnectionStatus.ERROR
                    _connectedDevice.value = null
                }
            } else {
                Log.e(TAG, "Failed to discover services, status: $status")
                closeGatt()
                _connectionStatus.value = ConnectionStatus.ERROR
                _connectedDevice.value = null
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Failed to write characteristic. Status: $status")
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _rssi.value = rssi
            }
        }
    }

    fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        if (!hasPermissions() || bluetoothAdapter == null) return
        val devices = bluetoothAdapter.bondedDevices.map {
            BluetoothDeviceInfo(it.name ?: "Dispositivo Desconocido", it.address)
        }
        _pairedDevices.value = devices
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (!hasPermissions() || bluetoothAdapter == null) return
        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "BLE scanner is null (is Bluetooth disabled?)")
            return
        }

        if (_isScanning.value) {
            stopScanning()
        }
        _scannedDevices.value = emptyList()
        _isScanning.value = true

        scanner.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!hasPermissions() || bluetoothAdapter == null) return
        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner != null && _isScanning.value) {
            scanner.stopScan(scanCallback)
        }
        _isScanning.value = false
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        if (!hasPermissions() || bluetoothAdapter == null) {
            _connectionStatus.value = ConnectionStatus.ERROR
            return
        }

        stopScanning()

        ioScope.launch {
            _connectionStatus.value = ConnectionStatus.CONNECTING
            val device = bluetoothAdapter.getRemoteDevice(address)
            _connectedDevice.value = BluetoothDeviceInfo(device.name ?: "Dispositivo Desconocido", address)

            withContext(Dispatchers.Main) {
                closeGatt()
                // Force BLE Transport using TRANSPORT_LE
                bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        rssiJob?.cancel()
        _rssi.value = null
        try {
            bluetoothGatt?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error calling gatt.disconnect", e)
        }
        try {
            bluetoothGatt?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error calling gatt.close", e)
        }
        bluetoothGatt = null
        targetCharacteristic = null
    }

    fun disconnect() {
        ioScope.launch {
            closeGatt()
            withContext(Dispatchers.Main) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _connectedDevice.value = null
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun write(data: String): Boolean {
        val gatt = bluetoothGatt ?: return false
        val characteristic = targetCharacteristic ?: return false
        val bytes = data.toByteArray()

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val status = gatt.writeCharacteristic(
                    characteristic,
                    bytes,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                )
                status == 0 // 0 means Success
            } else {
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                @Suppress("DEPRECATION")
                characteristic.value = bytes
                @Suppress("DEPRECATION")
                val success = gatt.writeCharacteristic(characteristic)
                success
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing BLE characteristic", e)
            false
        }
    }
}
