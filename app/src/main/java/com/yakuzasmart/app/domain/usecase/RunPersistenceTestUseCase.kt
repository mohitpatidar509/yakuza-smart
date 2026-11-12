package com.yakuzasmart.app.domain.usecase

import com.yakuzasmart.app.domain.model.PersistenceTestRecord
import com.yakuzasmart.app.domain.model.SettingCategory
import com.yakuzasmart.app.domain.model.SettingOption
import com.yakuzasmart.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class RunPersistenceTestUseCase(
    private val settingsRepository: SettingsRepository
) {
    val persistenceRecords: StateFlow<List<PersistenceTestRecord>> = settingsRepository.persistenceRecords

    suspend fun recordBaseline(category: SettingCategory, baseline: SettingOption?) {
        settingsRepository.recordPersistenceTestBaseline(category, baseline)
    }

    suspend fun recordWritten(
        category: SettingCategory,
        option: SettingOption,
        immediateAck: Boolean,
        beepHeard: Boolean
    ) {
        settingsRepository.recordPersistenceTestWritten(category, option, immediateAck, beepHeard)
    }

    suspend fun recordReconnected(
        category: SettingCategory,
        postValue: SettingOption?
    ): PersistenceTestRecord {
        return settingsRepository.recordPersistenceTestReconnected(category, postValue)
    }
}
