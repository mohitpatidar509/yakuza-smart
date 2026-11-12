package com.yakuzasmart.app.domain.repository

import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.domain.model.ScooterState
import kotlinx.coroutines.flow.StateFlow

interface ScooterRepository {
    val scooterState: StateFlow<ScooterState>
    val scannedDevices: StateFlow<List<BleDeviceItem>>
    val isScanning: StateFlow<Boolean>

    suspend fun startScan()
    fun stopScan()

    suspend fun connect(device: BleDeviceItem)
    suspend fun disconnect()

    fun setSimulatorEnabled(enabled: Boolean)
    fun isSimulatorEnabled(): Boolean

    suspend fun sendQueryAllStatus()
}
