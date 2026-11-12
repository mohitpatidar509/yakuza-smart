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
    val alarmTriggered: Boolean = false
) {
    val hasFault: Boolean
        get() = throttleError || motorHallError || overvoltage || undervoltage || overtemperature
}

data class ScooterState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedDevice: BleDeviceItem? = null,
    val isSimulated: Boolean = false,
    val lastSeenTimestamp: Long = 0L,

    // Real Telemetry Values (null when disconnected / unavailable)
    val speedKmh: Float? = null,
    val batteryPercent: Int? = null,
    val batteryVoltageVolts: Float? = null,
    val motorCurrentAmps: Float? = null,
    val controllerTempCelsius: Int? = null,
    val estimatedRangeKm: Int? = null,
    val faults: VehicleFaults = VehicleFaults(),

    // Current Controller Active Settings (as reported by controller)
    val activeStartupMode: StartupMode? = null,
    val activeSpeedGear: SpeedRegulationOption? = null,
    val activeCyclingMode: CyclingMode? = null,
    val activeVibrationSensitivity: VibrationSensitivity? = null,
    val activeSlopeParking: Boolean? = null,

    // Raw Controller Telemetry Counter
    val packetsReceivedCount: Long = 0L,
    val packetsTransmittedCount: Long = 0L
) {
    val isConnected: Boolean
        get() = connectionStatus == ConnectionStatus.CONNECTED_IDLE ||
                connectionStatus == ConnectionStatus.CONNECTED_SYNCING
}
