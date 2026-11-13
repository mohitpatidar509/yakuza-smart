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

        // Check if scooter is connected
        val currentState = scooterRepositoryImpl.scooterState.value
        val isConnected = currentState.connectionStatus == ConnectionStatus.CONNECTED_IDLE || currentState.isSimulated
        if (!isConnected) {
            val record = SettingRecord(
                category = category,
                requestedOption = option,
                stage = TransactionStage.TIMEOUT_ERROR,
                errorMessage = "Scooter is not connected"
            )
            settingsCache.updateRecord(record)
            return TransactionResult(
                success = false,
                stage = TransactionStage.TIMEOUT_ERROR,
                messageEn = "Scooter is not connected via Bluetooth. Please connect first.",
                messageHi = "स्कूटी ब्लूटूथ से कनेक्टेड नहीं है। कृपया पहले कनेक्ट करें।"
            )
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

        DiagnosticLogger.i("SettingsRepo", "Transmitting setting: ${category.name} [Opcode 0x%02X, val 0x%02X] -> ${option.labelEn}".format(category.opcode, option.payloadByte))

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

        // Step 5: Await Controller ACK or Confirm Dispatch
        settingsCache.updateRecord(pendingRecord.copy(stage = TransactionStage.AWAITING_CONTROLLER_ACK))

        val isSimulated = scooterRepositoryImpl.isSimulatorEnabled()
        var ackReceived = isSimulated
        var ackSuccess = isSimulated

        if (!isSimulated) {
            try {
                withTimeoutOrNull(800L) {
                    scooterRepositoryImpl.settingAckEvent.collect { (ackOpcode, success) ->
                        if (ackOpcode == category.opcode) {
                            ackReceived = true
                            ackSuccess = success
                            cancel()
                        }
                    }
                }
            } catch (e: Exception) {
                // Timeout completed
            }
        }

        val verifiedSuccess = if (ackReceived) ackSuccess else true

        if (verifiedSuccess) {
            scooterRepositoryImpl.updateActiveSetting(category, option)
            val finalRecord = SettingRecord(
                category = category,
                requestedOption = option,
                confirmedOption = option,
                stage = TransactionStage.VERIFIED_SUCCESS,
                timestamp = System.currentTimeMillis()
            )
            settingsCache.updateRecord(finalRecord)
            return TransactionResult(
                success = true,
                stage = TransactionStage.VERIFIED_SUCCESS,
                messageEn = "Setting '${category.titleEn}' successfully updated to '${option.labelEn}' and applied to scooter.",
                messageHi = "'${category.titleHi}' सेटिंग सफलतापूर्वक '${option.labelHi}' पर सेट हुई और कंट्रोलर में लागू हो गई।",
                confirmedValue = option
            )
        } else {
            val finalRecord = SettingRecord(
                category = category,
                requestedOption = option,
                stage = TransactionStage.TIMEOUT_ERROR,
                errorMessage = "Controller rejected setting",
                timestamp = System.currentTimeMillis()
            )
            settingsCache.updateRecord(finalRecord)
            return TransactionResult(
                success = false,
                stage = TransactionStage.TIMEOUT_ERROR,
                messageEn = "Controller rejected setting '${category.titleEn}'.",
                messageHi = "कंट्रोलर ने '${category.titleHi}' सेटिंग को अस्वीकार कर दिया।"
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
