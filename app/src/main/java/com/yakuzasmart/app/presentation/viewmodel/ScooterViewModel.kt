package com.yakuzasmart.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.data.local.AppPreferences
import com.yakuzasmart.app.data.repository.ScooterRepositoryImpl
import com.yakuzasmart.app.data.repository.SettingsRepositoryImpl
import com.yakuzasmart.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScooterViewModel(
    private val scooterRepository: ScooterRepositoryImpl,
    private val settingsRepository: SettingsRepositoryImpl,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val scooterState: StateFlow<ScooterState> = scooterRepository.scooterState
    val scannedDevices: StateFlow<List<BleDeviceItem>> = scooterRepository.scannedDevices
    val isScanning: StateFlow<Boolean> = scooterRepository.isScanning
    val isBluetoothEnabled: StateFlow<Boolean> = scooterRepository.isBluetoothEnabled
    val scanError: StateFlow<String?> = scooterRepository.scanError
    val savedScooter: StateFlow<BleDeviceItem?> = scooterRepository.savedScooter

    val settingRecords: StateFlow<Map<SettingCategory, SettingRecord>> = settingsRepository.settingRecords
    val persistenceRecords: StateFlow<List<PersistenceTestRecord>> = settingsRepository.persistenceRecords

    private val _language = MutableStateFlow(Language.ENGLISH)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _lastTransactionResult = MutableStateFlow<TransactionResult?>(null)
    val lastTransactionResult: StateFlow<TransactionResult?> = _lastTransactionResult.asStateFlow()

    private val _isExecutingTransaction = MutableStateFlow(false)
    val isExecutingTransaction: StateFlow<Boolean> = _isExecutingTransaction.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.languageFlow.collect { lang ->
                _language.value = lang
            }
        }
        viewModelScope.launch {
            appPreferences.isSimulatorEnabledFlow.collect { enabled ->
                scooterRepository.setSimulatorEnabled(enabled)
            }
        }
    }

    fun toggleLanguage() {
        val nextLang = if (_language.value == Language.ENGLISH) Language.HINDI else Language.ENGLISH
        _language.value = nextLang
        viewModelScope.launch {
            appPreferences.setLanguage(nextLang)
        }
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
        viewModelScope.launch {
            appPreferences.setLanguage(lang)
        }
    }

    fun startScan(): Boolean {
        var success = false
        viewModelScope.launch {
            success = scooterRepository.startScan()
        }
        return success
    }

    fun stopScan() {
        scooterRepository.stopScan()
    }

    fun connectDevice(device: BleDeviceItem) {
        viewModelScope.launch {
            scooterRepository.connect(device)
        }
    }

    fun connectSavedScooter() {
        viewModelScope.launch {
            scooterRepository.connectSavedScooter()
        }
    }

    fun forgetSavedScooter() {
        viewModelScope.launch {
            scooterRepository.forgetSavedScooter()
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            scooterRepository.disconnect()
        }
    }

    fun setSimulatorMode(enabled: Boolean) {
        scooterRepository.setSimulatorEnabled(enabled)
    }

    fun updateSetting(category: SettingCategory, option: SettingOption) {
        viewModelScope.launch {
            _isExecutingTransaction.value = true
            val speed = scooterState.value.speedKmh ?: 0.0f
            val result = settingsRepository.executeSettingTransaction(category, option, speed)
            _lastTransactionResult.value = result
            _isExecutingTransaction.value = false
        }
    }

    fun recordPersistenceBaseline(category: SettingCategory, baseline: SettingOption?) {
        viewModelScope.launch {
            settingsRepository.recordPersistenceTestBaseline(category, baseline)
        }
    }

    fun recordPersistenceWritten(
        category: SettingCategory,
        option: SettingOption,
        immediateAck: Boolean,
        beepHeard: Boolean
    ) {
        viewModelScope.launch {
            settingsRepository.recordPersistenceTestWritten(category, option, immediateAck, beepHeard)
        }
    }

    fun verifyPersistenceAfterReconnection(category: SettingCategory, postValue: SettingOption?) {
        viewModelScope.launch {
            settingsRepository.recordPersistenceTestReconnected(category, postValue)
        }
    }

    fun triggerSimulatedPowerCycle() {
        scooterRepository.triggerSimulatorPowerCycle()
    }

    fun clearCache() {
        settingsRepository.clearLocalCache()
    }
}
