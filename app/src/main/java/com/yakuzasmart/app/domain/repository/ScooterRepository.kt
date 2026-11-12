package com.yakuzasmart.app.domain.repository

import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.domain.model.YakuzaModel
import kotlinx.coroutines.flow.StateFlow

interface ScooterRepository {
    val scooterState: StateFlow<ScooterState>
    val scannedDevices: StateFlow<List<BleDeviceItem>>
    val isScanning: StateFlow<Boolean>
    val isBluetoothEnabled: StateFlow<Boolean>
    val scanError: StateFlow<String?>
    val savedScooter: StateFlow<BleDeviceItem?>

    suspend fun startScan(): Boolean
    fun stopScan()

    suspend fun connect(device: BleDeviceItem)
    suspend fun connectSavedScooter()
    suspend fun forgetSavedScooter()
    suspend fun disconnect()

    fun setSimulatorEnabled(enabled: Boolean)
    fun isSimulatorEnabled(): Boolean

    suspend fun sendQueryAllStatus()
    suspend fun setIgnition(powerOn: Boolean): Boolean
    suspend fun setVehicleLock(lock: Boolean): Boolean
    suspend fun findVehicle(): Boolean
    suspend fun openSeatCushion(): Boolean
    fun setVehicleModel(model: YakuzaModel)
}
