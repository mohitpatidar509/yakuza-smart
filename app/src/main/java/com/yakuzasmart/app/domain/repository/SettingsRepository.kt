package com.yakuzasmart.app.domain.repository

import com.yakuzasmart.app.domain.model.PersistenceTestRecord
import com.yakuzasmart.app.domain.model.SettingCategory
import com.yakuzasmart.app.domain.model.SettingOption
import com.yakuzasmart.app.domain.model.SettingRecord
import com.yakuzasmart.app.domain.model.TransactionResult
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settingRecords: StateFlow<Map<SettingCategory, SettingRecord>>
    val persistenceRecords: StateFlow<List<PersistenceTestRecord>>

    suspend fun executeSettingTransaction(
        category: SettingCategory,
        option: SettingOption,
        currentSpeedKmh: Float
    ): TransactionResult

    suspend fun recordPersistenceTestBaseline(category: SettingCategory, baseline: SettingOption?)
    suspend fun recordPersistenceTestWritten(category: SettingCategory, option: SettingOption, immediateAck: Boolean, beepHeard: Boolean)
    suspend fun recordPersistenceTestReconnected(category: SettingCategory, postValue: SettingOption?): PersistenceTestRecord

    fun clearLocalCache()
}
