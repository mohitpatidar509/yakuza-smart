package com.yakuzasmart.app

import com.yakuzasmart.app.core.security.InterlockResult
import com.yakuzasmart.app.core.security.SafetyInterlocks
import com.yakuzasmart.app.domain.model.StartupMode
import org.junit.Assert.*
import org.junit.Test

class SafetyInterlocksTest {

    @Test
    fun testVelocityGating_BlocksWhenMoving() {
        val result = SafetyInterlocks.canModifySetting(currentSpeedKmh = 15.0f, isHighRiskAction = false)
        assertTrue(result is InterlockResult.Blocked)
        val blocked = result as InterlockResult.Blocked
        assertTrue(blocked.reasonEn.contains("15.0 km/h"))
    }

    @Test
    fun testVelocityGating_AllowsWhenStationary() {
        val result = SafetyInterlocks.canModifySetting(currentSpeedKmh = 0.0f, isHighRiskAction = false)
        assertTrue(result is InterlockResult.Allowed)
    }

    @Test
    fun testHighRiskAction_RequiresConfirmationWhenStationary() {
        val result = SafetyInterlocks.canModifySetting(currentSpeedKmh = 0.0f, isHighRiskAction = true)
        assertTrue(result is InterlockResult.RequiresConfirmation)
    }
}
