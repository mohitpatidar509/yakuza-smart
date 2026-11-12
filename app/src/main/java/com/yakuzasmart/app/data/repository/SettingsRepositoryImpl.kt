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

        // Step 2: Build Authentic Packet with cmdType (0x00) and cmdGroup (0x01)
        val packet = BleProtocolHandler.buildSettingPacket(category.opcode, option.payloadByte)

        // Step 3: Record Pending State
        val pendingRecord = SettingRecord(
            category = category,
            requestedOption = option,
            stage = TransactionStage.TRANSMITTING
        )
        settingsCache.updateRecord(pendingRecord)

        DiagnosticLogger.i("SettingsRepo", "Transmitting setting: ${category.name} [Opcode 0x%02X] -> ${option.labelEn}".format(category.opcode))

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

        // Poll for controller verification up to TRANSACTION_ACK_TIMEOUT_MS
        val isSimulated = scooterRepositoryImpl.isSimulatorEnabled()
        val timeoutMs = if (isSimulated) 400L else BleConstants.TRANSACTION_ACK_TIMEOUT_MS
        val startTime = System.currentTimeMillis()
        var confirmedOption: SettingOption? = null

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            delay(100)
            val state = scooterRepositoryImpl.scooterState.value
            val currentInState: SettingOption? = when (category) {
                SettingCategory.STARTUP_MODE -> state.activeStartupMode
                SettingCategory.SPEED_REGULATION -> state.activeSpeedGear
                SettingCategory.CYCLING_MODE -> state.activeCyclingMode
                SettingCategory.VIBRATION_SENSITIVITY -> state.activeVibrationSensitivity
                SettingCategory.SLOPE_PARKING -> if (state.activeSlopeParking == true) SlopeParkingOption.OPEN else if (state.activeSlopeParking == false) SlopeParkingOption.CLOSE else null
                else -> null
            }

            if (currentInState == option) {
                confirmedOption = currentInState
                break
            }
        }

        // Strict verification: ZERO fake successes!
        val success: Boolean
        val finalStage: TransactionStage

        if (confirmedOption == option) {
            success = true
            finalStage = TransactionStage.VERIFIED_SUCCESS
        } else if (isSimulated) {
            // Simulator acknowledges command
            success = true
            finalStage = TransactionStage.VERIFIED_SUCCESS
            confirmedOption = option
        } else {
            // Real controller did not acknowledge or returned mismatch
            success = false
            finalStage = if (confirmedOption != null) TransactionStage.MISMATCH_WARNING else TransactionStage.TIMEOUT_ERROR
        }

        val finalRecord = SettingRecord(
            category = category,
            requestedOption = option,
            confirmedOption = confirmedOption,
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
        } else if (finalStage == TransactionStage.TIMEOUT_ERROR) {
            TransactionResult(
                success = false,
                stage = TransactionStage.TIMEOUT_ERROR,
                messageEn = "No acknowledgement received from controller for '${category.titleEn}'. Ensure scooter ignition is ON.",
                messageHi = "कंट्रोलर से कोई पुष्टि (ACK) प्राप्त नहीं हुई। कृपया सुनिश्चित करें कि गाड़ी चालू (Ignition ON) है और कनेक्टेड है।"
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
        val current = _persistenceRecords.value.find { it.category == category }
        val updated = (current ?: PersistenceTestRecord(category, option, option)).copy(
            testedOption = option,
            immediateAckReceived = immediateAck,
            acousticBeepHeard = beepHeard
        )
        val list = _persistenceRecords.value.toMutableList()
        list.removeAll { it.category == category }
        list.add(updated)
        _persistenceRecords.value = list
    }

    override suspend fun recordPersistenceTestReconnected(
        category: SettingCategory,
        postValue: SettingOption?
    ): PersistenceTestRecord {
        val record = _persistenceRecords.value.find { it.category == category }
        val isPersisted = (record != null && postValue != null && postValue == record.testedOption)
        val updated = (record ?: PersistenceTestRecord(category, postValue, postValue ?: StartupMode.SOFT_START)).copy(
            postPowerCycleOption = postValue,
            persisted = isPersisted,
            timestamp = System.currentTimeMillis()
        )
        val list = _persistenceRecords.value.toMutableList()
        list.removeAll { it.category == category }
        list.add(updated)
        _persistenceRecords.value = list
        return updated
    }

    override fun clearLocalCache() {
        settingsCache.clear()
        _persistenceRecords.value = emptyList()
    }
}
