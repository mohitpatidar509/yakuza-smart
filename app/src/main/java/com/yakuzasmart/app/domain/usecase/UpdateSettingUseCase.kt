package com.yakuzasmart.app.domain.usecase

import com.yakuzasmart.app.domain.model.SettingCategory
import com.yakuzasmart.app.domain.model.SettingOption
import com.yakuzasmart.app.domain.model.TransactionResult
import com.yakuzasmart.app.domain.repository.SettingsRepository

class UpdateSettingUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        category: SettingCategory,
        option: SettingOption,
        currentSpeedKmh: Float
    ): TransactionResult {
        return settingsRepository.executeSettingTransaction(category, option, currentSpeedKmh)
    }
}
