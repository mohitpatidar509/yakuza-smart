# BLE Protocol Research & Specification — Yakuza Smart

## 1. Overview
This specification documents the low-level Bluetooth Low Energy (BLE) communication protocol for Yakuza electric scooters, derived directly from disassembly and static analysis of the original `app-service.js` bundle extracted from `base.apk`.

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

*(Universal GATT discovery also supports `FFE0`/`FFE1`, `FEE0`, and Nordic UART fallback services).*

---

## 3. Frame Layout & Serialization

All frames transmitted over `0000FFF2` and received on `0000FFF1` follow the exact frame format:

### Structure Definition

```
Byte Index:   [0]      [1]      [2]      [3]        [4]         [5]      [6]        [7 ... N-2]       [N-1]
Field:        SYNC0    SYNC1    LEN      CMD_TYPE   CMD_GROUP   OPCODE   PARAM_LEN  PARAM_ARRAY       CHECKSUM
Value:        0x88     0x01     LEN_HEX  0x00       0x01/0x06   1 Byte   1 Byte     Variable          1 Byte
```

1. **Header / Sync Bytes:**
   - Byte 0: `0x88`
   - Byte 1: `0x01`
2. **Length Field (LEN):**
   - Byte 2: Total number of bytes from Byte 3 up to and including CHECKSUM:
     $$\text{LEN} = \text{Param Array Length} + 4$$
3. **Command Type (CMD_TYPE):**
   - Byte 3: `0x00`
4. **Command Group (CMD_GROUP):**
   - Byte 4: `0x01` for outbound control commands; `0x06` for inbound controller telemetry notifications.
5. **Opcode:**
   - Byte 5: Target command or register identifier.
6. **Parameter Length & Array:**
   - Byte 6: Sub-parameter length.
   - Bytes 7 to $(N-2)$: Payload parameters.
7. **Checksum:**
   - Byte $(N-1)$: Modulo-256 arithmetic sum of all bytes from Byte 0 through Byte $(N-2)$:
     $$\text{Checksum} = \left(\sum_{i=0}^{N-2} \text{Packet}[i]\right) \&\ 0\text{xFF}$$

---

## 4. Authentic Command Opcode Dictionary

| Opcode | Feature | Payload | Description |
| :--- | :--- | :--- | :--- |
| **`0x1B`** | **Ignition (Start / Stop)** | `[0x01, 0x01, 0x00, 0x00]` / `[0x01, 0x00, 0x00, 0x00]` | Start: Turns scooter ON. Stop: Turns scooter OFF. |
| **`0x1C`** | **Anti-Theft Lock (Alert)** | `[0x03, 0x01, 0x00, 0x00]` / `[0x03, 0x00, 0x00, 0x00]` | Lock: Arms anti-theft siren. Unlock: Disarms guard. |
| **`0x25`** | **Whistle / Siren (Find)** | `[0x01, 0x01]` | Triggers vehicle locator buzzer. |
| **`0x24`** | **Seat Solenoid (ZDS)** | `[0x01, 0x00]` | Releases electronic trunk/seat lock. |
| **`0x27`** | **Handshake / Query MAC** | `[0x00]` | Initial handshake sent upon BLE notify subscription. |
| **`0x3E`** | **Startup Mode (QDMS)** | `[0x01, 0x01]` / `[0x01, 0x00]` | `0x01` = Hard Boot (Instant Torque), `0x00` = Soft Start. |
| **`0x3D`** | **Slope Parking (PDZC)** | `[0x01, 0x01]` / `[0x01, 0x00]` | `0x01` = Hill Hold Active, `0x00` = Disabled. |
| **`0x3C`** | **Speed Regulation** | `[0x01, val]` | `0x00` (Default), `0x01` (15), `0x02` (25), `0x03` (100). |
| **`0x3B`** | **Vibration Sensitivity**| `[0x01, val]` | `0x00` (Low), `0x01` (Medium), `0x02` (High). |
| **`0x33`** | **Cycling Mode (QXMS)** | `[0x01, val]` | `0x00` (Comfort), `0x01` (Routine), `0x02` (Sport). |
| **`0x31`** | **Welcome Mode (YBMS)** | `[0x01, 0x01]` / `[0x01, 0x00]` | Courtesy startup greeting chime. |
| **`0x20`** | **Quiet / Mute Mode** | `[0x01, 0x01]` / `[0x01, 0x00]` | Mutes confirmation beeps. |
| **`0x21`** | **Child Mode (ETMS)** | `[0x01, 0x01]` / `[0x01, 0x00]` | Speed limiter to < 10 km/h. |
| **`0x29`** | **Turn Signal Tone** | `[0x01, 0x01]` / `[0x01, 0x00]` | Audible relay chime for turn indicators. |
| **`0x2A`** | **Password Lock** | `[0x01, 0x01]` / `[0x01, 0x00]` | Digital passcode unlock. |
| **`0x32`** | **Smart Helmet (ZNTK)** | `[0x01, 0x01]` / `[0x01, 0x00]` | Interlock with BLE helmet sensor. |
| **`0x38`** | **PCS** | `[0x01, 0x01]` / `[0x01, 0x00]` | Pre-collision proximity warning. |
| **`0x2D`** | **ABS** | `[0x01, 0x01]` / `[0x01, 0x00]` | Electronic anti-lock braking. |
| **`0x37`** | **TCS** | `[0x01, 0x01]` / `[0x01, 0x00]` | Traction control slip prevention. |
| **`0x30`** | **Voltage Configuration**| `[0x01, val]` | 48V, 60V, or 72V architecture selector. |
| **`0x51`** | **NFC Card Mode** | `[0x01, 0x01]` / `[0x01, 0x00]` | Card enrollment mode (`0x01`) or exit (`0x00`). |
| **`0x12`** | **Disconnect Notice** | `[0x00]` | Clean disconnection request. |

---

## 5. Inbound Telemetry Notifications (`cmdGroup = 0x06`)

### A. Primary Status Notification (`0x1F`)
Transmitted periodically by controller at 1–5 Hz:
- `paramArray[0]`: `0x01` = Started (Ignition ON), `0x00` = Standby (OFF)
- `paramArray[5]`: `0x01` = Anti-theft Guard Armed (Locked)
- `paramArray[13]`: Speed in km/h (`parseInt(hex, 16)`)
- `paramArray[16]`: Battery State-of-Charge (0–100%)
- `paramArray[17..20]`: Total Odometer in km
- `paramArray[21..24]`: Trip Meter in km
- `paramArray[26]`: Faults Bitmask:
  - Bit 2: Controller Error
  - Bit 3: Motor Hall Sensor Fault
  - Bit 4: Undervoltage Alarm
  - Bit 5: Throttle / Plate Fault
  - Bit 6: Seat Lock Open
  - Bit 7: ECU Fault
- `paramArray[28]`: Seat Lock Status
- `paramArray[32]`: Mute Mode Status
- `paramArray[33]`: Child Mode Status

### B. Electrical Telemetry Notification (`0x2F`)
- `paramArray[9]`: Active Cycling Mode
- `paramArray[10]`: Motor Current (Amperes * 10)
- `paramArray[11]`: Maximum Temperature (°C)
- `paramArray[13]`: MOS Temperature (°C)
- `paramArray[15]`: Charging Status (`0x01` = Charging)
- `paramArray[16..17]`: Cell / Pack Voltage ($mV \times 0.001$)
