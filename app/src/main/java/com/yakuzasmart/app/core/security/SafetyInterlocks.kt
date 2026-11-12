package com.yakuzasmart.app.core.security

object SafetyInterlocks {
    /**
     * Checks if a setting change can be safely performed given current telemetry speed.
     * Vehicle parameters affecting motor response, parking brake, or limiter cannot be altered while moving.
     */
    fun canModifySetting(currentSpeedKmh: Float, isHighRiskAction: Boolean = false): InterlockResult {
        return when {
            currentSpeedKmh > 0.5f -> {
                InterlockResult.Blocked(
                    reasonEn = "Blocked for safety: Vehicle is currently moving ($currentSpeedKmh km/h). Park the scooter to change settings.",
                    reasonHi = "सुरक्षा हेतु अवरुद्ध: वाहन अभी गति में है ($currentSpeedKmh km/h)। सेटिंग बदलने के लिए स्कूटर रोकें।"
                )
            }
            isHighRiskAction -> {
                InterlockResult.RequiresConfirmation(
                    promptEn = "This action will affect vehicle drive dynamics. Ensure scooter is on its dual center stand.",
                    promptHi = "यह क्रिया वाहन ड्राइव डायनामिक्स को प्रभावित करेगी। सुनिश्चित करें कि स्कूटर मेन स्टैंड पर है।"
                )
            }
            else -> InterlockResult.Allowed
        }
    }
}

sealed class InterlockResult {
    data object Allowed : InterlockResult()
    data class RequiresConfirmation(val promptEn: String, val promptHi: String) : InterlockResult()
    data class Blocked(val reasonEn: String, val reasonHi: String) : InterlockResult()
}
