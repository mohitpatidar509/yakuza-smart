package com.yakuzasmart.app.domain.usecase

import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.domain.repository.ScooterRepository
import kotlinx.coroutines.flow.StateFlow

class ConnectScooterUseCase(
    private val scooterRepository: ScooterRepository
) {
    val scooterState: StateFlow<ScooterState> = scooterRepository.scooterState

    suspend fun connect(device: BleDeviceItem) {
        scooterRepository.connect(device)
    }

    suspend fun disconnect() {
        scooterRepository.disconnect()
    }

    fun setSimulatorMode(enabled: Boolean) {
        scooterRepository.setSimulatorEnabled(enabled)
    }

    fun isSimulatorMode(): Boolean = scooterRepository.isSimulatorEnabled()
}
