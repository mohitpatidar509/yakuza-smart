package com.yakuzasmart.app.domain.model

data class BleDeviceItem(
    val name: String?,
    val address: String,
    val rssi: Int,
    val isBonded: Boolean = false,
    val isYakuzaDevice: Boolean = false
) {
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: "Unknown Device ($address)"

    val signalBars: Int
        get() = when {
            rssi >= -60 -> 4
            rssi >= -75 -> 3
            rssi >= -85 -> 2
            rssi >= -95 -> 1
            else -> 0
        }
}
