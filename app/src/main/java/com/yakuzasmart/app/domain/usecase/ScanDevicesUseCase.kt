package com.yakuzasmart.app.domain.usecase

import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.domain.repository.ScooterRepository
import kotlinx.coroutines.flow.StateFlow

class ScanDevicesUseCase(
    private val scooterRepository: ScooterRepository
) {
    val scannedDevices: StateFlow<List<BleDeviceItem>> = scooterRepository.scannedDevices
    val isScanning: StateFlow<Boolean> = scooterRepository.isScanning

    suspend fun startScan() {
        scooterRepository.startScan()
    }

    fun stopScan() {
        scooterRepository.stopScan()
    }
}
