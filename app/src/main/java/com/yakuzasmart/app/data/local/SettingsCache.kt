package com.yakuzasmart.app.data.local

import com.yakuzasmart.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsCache {
    private val _records = MutableStateFlow<Map<SettingCategory, SettingRecord>>(
        mapOf(
            SettingCategory.STARTUP_MODE to SettingRecord(
                category = SettingCategory.STARTUP_MODE,
                requestedOption = StartupMode.SOFT_START,
                stage = TransactionStage.IDLE
            ),
            SettingCategory.SPEED_REGULATION to SettingRecord(
                category = SettingCategory.SPEED_REGULATION,
                requestedOption = SpeedRegulationOption.SPEED_0,
                stage = TransactionStage.IDLE
            ),
            SettingCategory.VIBRATION_SENSITIVITY to SettingRecord(
                category = SettingCategory.VIBRATION_SENSITIVITY,
                requestedOption = VibrationSensitivity.MEDIUM,
                stage = TransactionStage.IDLE
            ),
            SettingCategory.CYCLING_MODE to SettingRecord(
                category = SettingCategory.CYCLING_MODE,
                requestedOption = CyclingMode.COMFORT,
                stage = TransactionStage.IDLE
            ),
            SettingCategory.SLOPE_PARKING to SettingRecord(
                category = SettingCategory.SLOPE_PARKING,
                requestedOption = SlopeParkingOption.CLOSE,
                stage = TransactionStage.IDLE
            )
        )
    )
    val records: StateFlow<Map<SettingCategory, SettingRecord>> = _records.asStateFlow()

    fun updateRecord(record: SettingRecord) {
        val current = _records.value.toMutableMap()
        current[record.category] = record
        _records.value = current
    }

    fun getRecord(category: SettingCategory): SettingRecord? = _records.value[category]

    fun clear() {
        _records.value = emptyMap()
    }
}
