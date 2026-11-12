# Test Report & Quality Assurance Suite — Yakuza Smart

## 1. Overview
This test report summarizes the testing suite, unit verification, simulator validation, and stationary hardware testing plan for the Yakuza Smart native Android application.

---

## 2. Test Verification Matrix

| Test Suite | File / Scope | Test Cases Executed | Result | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Protocol Packet Framing** | `BleProtocolHandlerTest.kt` | Opcode serialization, framing length, checksum calculation (Modulo-256 and XOR) | 5 / 5 PASS | Implemented & Unit Tested |
| **Corrupt & Truncated Packets** | `BleProtocolHandlerTest.kt` | Corrupted checksum rejection, short frame rejection, wrong preamble rejection | 3 / 3 PASS | Implemented & Unit Tested |
| **Telemetry Parsing** | `BleProtocolHandlerTest.kt` | Speed (0.1 km/h resolution), battery percentage, voltage (0.1 V), motor current, controller temp, fault bitmask | 4 / 4 PASS | Implemented & Unit Tested |
| **Safety Interlocks** | `SafetyInterlocksTest.kt` | Velocity gating (>0.5 km/h blocks writes), stationary check, confirmation requirement | 3 / 3 PASS | Implemented & Unit Tested |
| **BLE Simulator State Machine** | `BleSimulatorTest.kt` | Virtual telemetry stream (1 Hz), command ACK responses, volatile SRAM power-cycle reset | 3 / 3 PASS | Implemented & Simulator Tested |
| **Setting Transactions** | `SettingsRepositoryImpl.kt` | 4-stage transaction: Validate -> Dispatch -> Wait ACK -> Read-back -> Cache | Verified via Simulator | Implemented & Simulator Tested |
| **Real Scooter Hardware Test** | Stationary Physical Scooter | Dual center-stand testing of Startup Mode, Speed Regulation, and Persistence | Pending Physical Access | Test Plan Documented |

---

## 3. Detailed Test Suites

### Suite A: Binary Protocol & Serialization
- **Packet Generation:** Evaluates that `BleProtocolHandler.buildPacket(opcode, payload)` generates valid buffers prefixed with `0x88 0x01`, calculates correct payload lengths, and appends the exact modulo-256 arithmetic sum.
- **Frame Validation:** Rejects any inbound byte stream that does not match the 2-byte preamble, declared length, or computed checksum.
- **Telemetry Extraction:** Verifies that raw 16-byte status notifications convert accurately to user-facing metrics with appropriate unit scaling (e.g., speed integer 300 converts to `30.0 km/h`, temperature offset of 40°C is correctly subtracted).

### Suite B: Safety Interlock Enforcement
- **Motion Blocking:** Programmatically asserts that attempts to modify motor-sensitive parameters (Startup Mode, Speed Limit, Hill Hold) when `speedKmh > 0.5f` immediately abort with an `InterlockResult.Blocked` exception, preventing dangerous mid-ride reconfigurations.

### Suite C: In-Memory Controller Simulation
- **Power-Cycle Emulation:** Validates that `BleSimulator.simulatePowerCycle()` resets in-memory variables to factory defaults (`Soft Start`, `Gear 0`, `Medium Vibration`), replicating the real-world scooter firmware limitation and allowing full user training within the Diagnostics screen without physical hardware.

---

## 4. Physical Scooter Stationary Test Checklist (शारीरिक परीक्षण चेकलिस्ट)

1. **Center Stand Engagement:** The scooter must be securely hoisted onto its center stand with the drive wheel completely elevated.
2. **Throttle Inactivity:** Verify zero throttle input before initiating BLE pairing.
3. **Single Setting Modification:** Alter one setting (e.g. Startup Mode: Hard Boot), confirm the acoustic chime, and observe the app badge transition to `VERIFIED`.
4. **Controlled Power Discharge:** Turn off the ignition key, wait 15 seconds for capacitors to discharge, and switch back ON.
5. **Reconnection Read-back:** Reconnect in the Diagnostics screen and record whether the register retained `Hard Boot` or reverted to `Soft Start`.
