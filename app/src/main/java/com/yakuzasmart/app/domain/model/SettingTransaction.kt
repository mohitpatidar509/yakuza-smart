package com.yakuzasmart.app.domain.model

enum class TransactionStage {
    IDLE,
    VALIDATING_SAFETY,
    TRANSMITTING,
    AWAITING_CONTROLLER_ACK,
    READING_BACK,
    VERIFIED_SUCCESS,
    MISMATCH_WARNING,
    TIMEOUT_ERROR,
    BLOCKED_BY_SAFETY
}

data class SettingRecord(
    val category: SettingCategory,
    val requestedOption: SettingOption,
    val confirmedOption: SettingOption? = null,
    val stage: TransactionStage = TransactionStage.IDLE,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val isVerified: Boolean
        get() = stage == TransactionStage.VERIFIED_SUCCESS

    val hasMismatch: Boolean
        get() = stage == TransactionStage.MISMATCH_WARNING
}

data class TransactionResult(
    val success: Boolean,
    val stage: TransactionStage,
    val messageEn: String,
    val messageHi: String,
    val confirmedValue: SettingOption? = null
)
