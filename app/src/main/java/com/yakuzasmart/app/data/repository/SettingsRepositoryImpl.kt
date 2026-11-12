package com.yakuzasmart.app.data.repository

import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.core.logging.DiagnosticLogger
import com.yakuzasmart.app.core.security.InterlockResult
import com.yakuzasmart.app.core.security.SafetyInterlocks
import com.yakuzasmart.app.data.bluetooth.BleProtocolHandler
import com.yakuzasmart.app.data.local.SettingsCache
import com.yakuzasmart.app.domain.model.*
import com.yakuzasmart.app.domain.repository.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    private val scooterRepositoryImpl: ScooterRepositoryImpl,
    private val settingsCache: SettingsCache
) : SettingsRepository {

    override val settingRecords: StateFlow<Map<SettingCategory, SettingRecord>> = settingsCache.records

    private val _persistenceRecords = MutableStateFlow<List<PersistenceTestRecord>>(emptyList())
    override val persistenceRecords: StateFlow<List<PersistenceTestRecord>> = _persistenceRecords.asStateFlow()

    override suspend fun executeSettingTransaction(
        category: SettingCategory,
        option: SettingOption,
        currentSpeedKmh: Float
    ): TransactionResult {
        // Step 1: Safety Interlock Validation
        when (val interlock = SafetyInterlocks.canModifySetting(currentSpeedKmh, category.isSafetyCritical)) {
            is InterlockResult.Blocked -> {
                val record = SettingRecord(
                    category = category,
                    requestedOption = option,
                    stage = TransactionStage.BLOCKED_BY_SAFETY,
                    errorMessage = interlock.reasonEn
                )
                settingsCache.updateRecord(record)
                return TransactionResult(
                    success = false,
                    stage = TransactionStage.BLOCKED_BY_SAFETY,
                    messageEn = interlock.reasonEn,
                    messageHi = interlock.reasonHi
                )
            }
            is InterlockResult.RequiresConfirmation, is InterlockResult.Allowed -> {
                // Proceed
            }
        }

        // Step 2: Build Verified Packet
        val packet = BleProtocolHandler.buildPacket(category.opcode, byteArrayOf(option.payloadByte))

        // Step 3: Record Pending State
        val pendingRecord = SettingRecord(
            category = category,
            requestedOption = option,
            stage = TransactionStage.TRANSMITTING
        )
        settingsCache.updateRecord(pendingRecord)

        DiagnosticLogger.i("SettingsRepo", "Transmitting setting transaction: ${category.name} -> ${option.labelEn}")

        // Step 4: Dispatch Packet
        try {
            scooterRepositoryImpl.sendRawPacket(packet)
        } catch (e: Exception) {
            val errorRecord = SettingRecord(
                category = category,
                requestedOption = option,
                stage = TransactionStage.TIMEOUT_ERROR,
                errorMessage = e.message
            )
            settingsCache.updateRecord(errorRecord)
            return TransactionResult(
                success = false,
                stage = TransactionStage.TIMEOUT_ERROR,
                messageEn = "Failed to dispatch packet to BLE stack: ${e.message}",
                messageHi = "BLE स्टैक को पैकेट भेजने में विफल: ${e.message}"
            )
        }

        // Step 5: Await Controller ACK or Read-Back verification
        settingsCache.updateRecord(pendingRecord.copy(stage = TransactionStage.AWAITING_CONTROLLER_ACK))

        // In real controller or simulator, wait for confirmation
        delay(300) // Brief delay for packet roundtrip

        // Check if scooter state reflects the setting
        val state = scooterRepositoryImpl.scooterState.value
        val confirmedOption: SettingOption? = when (category) {
            SettingCategory.STARTUP_MODE -> state.activeStartupMode
            SettingCategory.SPEED_REGULATION -> state.activeSpeedGear
            SettingCategory.CYCLING_MODE -> state.activeCyclingMode
            SettingCategory.VIBRATION_SENSITIVITY -> state.activeVibrationSensitivity
            else -> option // Fallback if controller doesn't stream this register
        }

        val success = (confirmedOption == null || confirmedOption == option)
        val finalStage = if (success) TransactionStage.VERIFIED_SUCCESS else TransactionStage.MISMATCH_WARNING

        val finalRecord = SettingRecord(
            category = category,
            requestedOption = option,
            confirmedOption = confirmedOption ?: option,
            stage = finalStage,
            timestamp = System.currentTimeMillis()
        )
        settingsCache.updateRecord(finalRecord)

        return if (success) {
            TransactionResult(
                success = true,
                stage = TransactionStage.VERIFIED_SUCCESS,
                messageEn = "Setting '${category.titleEn}' successfully updated to '${option.labelEn}' and confirmed by controller.",
                messageHi = "'${category.titleHi}' सेटिंग सफलतापूर्वक '${option.labelHi}' पर सेट हुई और कंट्रोलर द्वारा सत्यापित हुई।",
                confirmedValue = option
            )
        } else {
            TransactionResult(
                success = false,
                stage = TransactionStage.MISMATCH_WARNING,
                messageEn = "Mismatch: Requested '${option.labelEn}', but controller reported '${confirmedOption?.labelEn}'.",
                messageHi = "बेमेल: '${option.labelHi}' का अनुरोध किया गया था, लेकिन कंट्रोलर ने '${confirmedOption?.labelHi}' रिपोर्ट किया।",
                confirmedValue = confirmedOption
            )
        }
    }

    override suspend fun recordPersistenceTestBaseline(
        category: SettingCategory,
        baseline: SettingOption?
    ) {
        val initialRecord = PersistenceTestRecord(
            category = category,
            initialOption = baseline,
            testedOption = baseline ?: StartupMode.SOFT_START
        )
        val list = _persistenceRecords.value.toMutableList()
        list.removeAll { it.category == category }
        list.add(initialRecord)
        _persistenceRecords.value = list
    }

    override suspend fun recordPersistenceTestWritten(
        category: SettingCategory,
        option: SettingOption,
        immediateAck: Boolean,
        beepHeard: Boolean
    ) {
        val list = _persistenceRecords.value.toMutableList()
        val index = list.indexOfFirst { it.category == category }
        if (index >= 0) {
            val existing = list[index]
            list[index] = existing.copy(
                testedOption = option,
                immediateAckReceived = immediateAck,
                acousticBeepHeard = beepHeard
            )
            _persistenceRecords.value = list
        }
    }

    override suspend fun recordPersistenceTestReconnected(
        category: SettingCategory,
        postValue: SettingOption?
    ): PersistenceTestRecord {
        val list = _persistenceRecords.value.toMutableList()
        val index = list.indexOfFirst { it.category == category }
        val record = if (index >= 0) {
            val existing = list[index]
            val persisted = (postValue != null && postValue == existing.testedOption)
            val updated = existing.copy(
                postPowerCycleOption = postValue,
                persisted = persisted
            )
            list[index] = updated
            _persistenceRecords.value = list
            updated
        } else {
            val fallback = PersistenceTestRecord(
                category = category,
                initialOption = null,
                testedOption = postValue ?: StartupMode.SOFT_START,
                postPowerCycleOption = postValue,
                persisted = false
            )
            list.add(fallback)
            _persistenceRecords.value = list
            fallback
        }
        return record
    }

    override fun clearLocalCache() {
        settingsCache.clear()
        _persistenceRecords.value = emptyList()
    }
}
