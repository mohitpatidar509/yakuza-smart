package com.yakuzasmart.app.domain.model

enum class PersistenceTestStep {
    NOT_STARTED,
    STEP_1_RECORD_BASELINE,
    STEP_2_MODIFY_PARAMETER,
    STEP_3_POWER_OFF_WAIT,
    STEP_4_RECONNECT_VERIFY,
    EVALUATION_COMPLETE
}

data class PersistenceTestRecord(
    val category: SettingCategory,
    val initialOption: SettingOption?,
    val testedOption: SettingOption,
    val immediateAckReceived: Boolean = false,
    val acousticBeepHeard: Boolean = false,
    val postPowerCycleOption: SettingOption? = null,
    val persisted: Boolean? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val resultSummaryEn: String
        get() = when (persisted) {
            true -> "PASS: Setting persisted in Non-Volatile Memory (Flash/EEPROM)."
            false -> "FAIL: Setting reset to RAM default after power-cycle. Firmware volatile limitation."
            null -> "Pending power-cycle re-check."
        }

    val resultSummaryHi: String
        get() = when (persisted) {
            true -> "सफल: पावर बंद होने के बाद भी सेटिंग मेमोरी (Flash/EEPROM) में सुरक्षित रही।"
            false -> "असफल: पावर बंद होने पर सेटिंग रीसेट हो गई (कंट्रोलर में वोलेटाइल RAM सीमा)।"
            null -> "पावर-साइकिल सत्यापन लंबित है।"
        }
}
