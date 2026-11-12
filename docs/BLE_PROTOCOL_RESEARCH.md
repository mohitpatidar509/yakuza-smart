# BLE Protocol Research & Specification — Yakuza Smart

## 1. Overview
This specification documents the low-level Bluetooth Low Energy (BLE) communication protocol for Yakuza electric scooters, derived from static analysis of the original application, telemetry reverse engineering, and standard industrial e-mobility controller implementations.

---

## 2. Physical & GATT Layer Parameters

- **Transport:** Bluetooth Low Energy (Bluetooth 4.2 / 5.0 Core Specification)
- **MTU Size:** Default 23 bytes (Negotiate up to 247 bytes if supported by phone and controller)
- **Connection Interval:** 20 ms to 50 ms recommended for low-latency telemetry
- **Advertising Interval:** ~100 ms

### GATT UUID Table

| Identifier | Full 128-bit UUID | Properties | Description |
| :--- | :--- | :--- | :--- |
| **Primary Service** | `0000FFF0-0000-1000-8000-00805F9B34FB` | Primary | Vehicle Telemetry and Parameter Service |
| **Notify Characteristic** | `0000FFF1-0000-1000-8000-00805F9B34FB` | `NOTIFY`, `READ` | Telemetry notifications, command ACKs |
| **Write Characteristic** | `0000FFF2-0000-1000-8000-00805F9B34FB` | `WRITE`, `WRITE_NO_RESPONSE` | Outbound commands and queries |
| **CCCD Descriptor** | `00002902-0000-1000-8000-00805F9B34FB` | `READ`, `WRITE` | Client Characteristic Configuration (`0x0001` to enable) |

---

## 3. Frame Layout & Serialization

All frames transmitted over `0000FFF2` and received on `0000FFF1` follow a structured packet format:

### Structure Definition

```
Byte Index:   [0]      [1]      [2]      [3]      [4 ... N-2]       [N-1]
Field:        SYNC0    SYNC1    LEN      OPCODE   PAYLOAD           CHECKSUM
Value:        0x88     0x01     0x04-0x10 1 Byte  Variable (0-11 B) 1 Byte
```

1. **Preamble / Magic Header:**
   - Byte 0: `0x88` (Static magic sync byte)
   - Byte 1: `0x01` (Protocol version / target device ID)
2. **Length Field (LEN):**
   - Byte 2: Total number of bytes from Byte 2 up to and including the CHECKSUM byte:
     $$\text{LEN} = \text{Payload Length} + 3$$
3. **Opcode (Command ID):**
   - Byte 3: Specifies target configuration register or query action.
4. **Payload (Data Bytes):**
   - Bytes 4 through $(N-2)$: The specific setting parameter, integer value, or payload string.
5. **Checksum (CRC/Sum):**
   - Byte $(N-1)$: Modulo-256 arithmetic sum of all preceding bytes:
     $$\text{Checksum} = \left(\sum_{i=0}^{N-2} \text{Packet}[i]\right) \&\ 0\text{xFF}$$
   *(Note: Secondary firmware variant uses bitwise XOR across Byte 0 to Byte N-2. The parser supports both).*

---

## 4. Command Opcode Dictionary

| Opcode | Setting / Feature | Payload Size | Valid Payload Values | Expected ACK / Response |
| :--- | :--- | :--- | :--- | :--- |
| **`0x01`** | **Query Status (ReadAll)** | 0 Bytes | None | 16-byte Telemetry packet |
| **`0x3E`** | **Startup Mode** | 1 Byte | `0x00` (Soft Start), `0x01` (Hard Boot) | `[0x88, 0x01, 0x04, 0x3E, val, chk]` |
| **`0x3A`** | **Speed Regulation** | 1 Byte | `0x00` (0/Default), `0x01` (15), `0x02` (25), `0x03` (100) | ACK with echoed opcode `0x3A` |
| **`0x42`** | **Vibration Sensitivity**| 1 Byte | `0x01` (Low), `0x02` (Medium), `0x03` (High) | ACK with echoed opcode `0x42` |
| **`0x45`** | **Cycling Mode** | 1 Byte | `0x00` (Comfort), `0x01` (Cross-country), `0x02` (Cross-country Plus), `0x03` (Racing) | ACK with echoed opcode `0x45` |
| **`0x51`** | **Slope Parking** | 1 Byte | `0x01` (Open / Enabled), `0x00` (Close / Disabled) | ACK with echoed opcode `0x51` |
| **`0x60`** | **Child Mode** | 1 Byte | `0x01` (On), `0x00` (Off) | ACK with echoed opcode `0x60` |
| **`0x61`** | **Welcome Mode** | 1 Byte | `0x01` (On), `0x00` (Off) | ACK with echoed opcode `0x61` |
| **`0x62`** | **Quiet Mode** | 1 Byte | `0x01` (On), `0x00` (Off) | ACK with echoed opcode `0x62` |
| **`0x70`** | **Smart Helmet Interlock**| 1 Byte | `0x01` (Enabled), `0x00` (Disabled) | ACK with echoed opcode `0x70` |
| **`0x75`** | **Turn Signal Tone** | 1 Byte | `0x01` (On), `0x00` (Off) | ACK with echoed opcode `0x75` |
| **`0x80`** | **NFC Card Command** | 1 Byte | `0x01` (Enroll / Read), `0x02` (Clear All) | Response status byte |
| **`0xEE`** | **Commit / Save to Flash** *(Experimental hypothesis)* | 2 Bytes | `0x55, 0xAA` (Unlock key pattern) | Confirmation byte |

---

## 5. Inbound Telemetry Frame Specification

When notifications are subscribed on `0000FFF1`, the controller transmits periodic telemetry frames (at 1 Hz to 5 Hz when active):

```
Byte 0-1:  0x88 0x01 (Header)
Byte 2:    0x10 (Length = 16 bytes)
Byte 3:    0x01 (Status Telemetry Opcode)
Byte 4:    Speed High Byte (km/h * 10)
Byte 5:    Speed Low Byte
Byte 6:    Battery State-of-Charge (0 - 100%)
Byte 7:    Battery Voltage High Byte (mV / 100)
Byte 8:    Battery Voltage Low Byte
Byte 9:    Motor Current (Amperes * 10)
Byte 10:   Controller Temperature (°C + 40 offset)
Byte 11:   Fault / Status Bitmask 1:
           - Bit 0: Kickstand Down (1 = Down / Motor Cut)
           - Bit 1: Brake Engaged (1 = Active)
           - Bit 2: Throttle Error
           - Bit 3: Motor Hall Error
           - Bit 4: Controller Overvoltage
           - Bit 5: Controller Undervoltage
           - Bit 6: Over-temperature Alarm
           - Bit 7: Anti-theft Alarm Triggered
Byte 12:   Active Startup Mode (0 = Soft, 1 = Hard)
Byte 13:   Active Speed Gear (0, 1, 2, 3)
Byte 14:   Active Cycling Mode (0, 1, 2, 3)
Byte 15:   Checksum (Sum of Bytes 0..14 & 0xFF)
```

---

## 6. Safety & Verification Rules

1. **Zero Guessing on Real Vehicles:**
   - Do NOT send arbitrary hex sequences to a connected scooter while the vehicle is in motion or on public roads.
   - All packet tests must be conducted while the scooter is parked securely on its dual center stand with the drive wheel elevated.
2. **Read-Back Verification Requirement:**
   - Every outbound setting write must await an inbound notification with the matching opcode within a 2000 ms timeout window.
   - If no ACK is received, the app transitions the setting state to `UNCONFIRMED_TIMEOUT`.
3. **Built-in Mock / Simulator Mode:**
   - The native app includes a fully conformant in-memory BLE mock controller (`BleSimulator`).
   - The simulator emulates the `0x88 0x01` protocol, emits telemetry at 1 Hz, responds to all opcodes, and can simulate volatile SRAM power-cycle resets to validate the app's diagnostic persistence workflow.
