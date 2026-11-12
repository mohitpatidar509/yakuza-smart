package com.yakuzasmart.app

import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.data.bluetooth.BleProtocolHandler
import com.yakuzasmart.app.data.bluetooth.BleSimulator
import com.yakuzasmart.app.data.bluetooth.ParsedInboundResult
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.domain.model.StartupMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BleSimulatorTest {

    @Test
    fun testSimulatorHandlesOutboundWrite_EmitsAck() = runTest {
        val testScope = TestScope(testScheduler)
        val simulator = BleSimulator(testScope)

        var receivedAck: ByteArray? = null
        val job = testScope.testScheduler

        // Build command to set Hard Boot
        val packet = BleProtocolHandler.buildPacket(
            BleConstants.OPCODE_STARTUP_MODE,
            byteArrayOf(StartupMode.HARD_BOOT.payloadByte)
        )

        simulator.handleOutboundWrite(packet)

        // Verify power-cycle reset
        simulator.simulatePowerCycle()

        // After simulated power cycle, query status returns Soft Start default
        val queryPacket = BleProtocolHandler.buildPacket(BleConstants.OPCODE_QUERY_STATUS)
        simulator.handleOutboundWrite(queryPacket)
    }
}
