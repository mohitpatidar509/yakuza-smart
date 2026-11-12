package com.yakuzasmart.app.domain.model

enum class ConnectionStatus {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    DISCOVERING_SERVICES,
    CONNECTED_IDLE,
    CONNECTED_SYNCING
}

data class VehicleFaults(
    val kickstandDown: Boolean = false,
    val brakeEngaged: Boolean = false,
    val throttleError: Boolean = false,
    val motorHallError: Boolean = false,
    val overvoltage: Boolean = false,
    val undervoltage: Boolean = false,
    val overtemperature: Boolean = false,
    val alarmTriggered: Boolean = false,
    val controllerError: Boolean = false
) {
    val hasFault: Boolean
        get() = throttleError || motorHallError || overvoltage || undervoltage || overtemperature || controllerError
}

data class ScooterState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedDevice: BleDeviceItem? = null,
    val isSimulated: Boolean = false,
    val lastSeenTimestamp: Long = 0L,

    // Real Remote Control & Vehicle State (From 0x1F notification)
    val isIgnitionOn: Boolean = false,
    val isLocked: Boolean = false,
    val isCushionOpen: Boolean = false,
    val isChildModeOn: Boolean = false,
    val isMuteOn: Boolean = false,

    // Real Telemetry Values (null when disconnected / waiting for controller data)
    val speedKmh: Float? = null,
    val batteryPercent: Int? = null,
    val batteryVoltageVolts: Float? = null,
    val motorCurrentAmps: Float? = null,
    val controllerTempCelsius: Int? = null,
    val estimatedRangeKm: Int? = null,
    val totalMileageKm: Float? = null,
    val tripMileageKm: Float? = null,
    val isCharging: Boolean = false,
    val faults: VehicleFaults = VehicleFaults(),

    // Current Controller Active Settings (as reported by controller)
    val activeStartupMode: StartupMode? = null,
    val activeSpeedGear: SpeedRegulationOption? = null,
    val activeCyclingMode: CyclingMode? = null,
    val activeVibrationSensitivity: VibrationSensitivity? = null,
    val activeSlopeParking: Boolean? = null,

    // Vehicle Hardware Profile
    val selectedModel: YakuzaModel = YakuzaModel.YAKUZA_RUBIE,

    // Raw Controller Telemetry Counter
    val packetsReceivedCount: Long = 0L,
    val packetsTransmittedCount: Long = 0L
) {
    val isConnected: Boolean
        get() = connectionStatus == ConnectionStatus.CONNECTED_IDLE ||
                connectionStatus == ConnectionStatus.CONNECTED_SYNCING
}
