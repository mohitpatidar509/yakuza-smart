# APK Technical Audit — Yakuza Reference Application

## 1. Executive Summary
This technical audit analyzes the reference Android APK (`Yakuza.apk`) for Yakuza electric scooters. The original companion application was engineered to provide smartphone connectivity, telemetry display, and configuration over Bluetooth Low Energy (BLE).

This document details the architectural structure, communication pipeline, protocol framing, persistence defects, and security posture of the legacy app to guide the native Kotlin/Jetpack Compose redevelopment.

---

## 2. APK Metadata & Framework Architecture

### Core Metadata
- **Application Label:** Yakuza (याकुज़ा स्मार्ट)
- **Framework Type:** DCloud Uni-App (Vue.js 2.x / WebView runtime wrapped inside Android shell)
- **Native Container:** DCloud HBuilder Native Android SDK (`io.dcloud.feature.internal` / `io.dcloud.js.ble`)
- **App ID / Namespace:** `__UNI__F0B...` / `io.dcloud.HBuilder`
- **Target Android SDK:** API Level 30 / 31 (Android 11/12)
- **Minimum Android SDK:** API Level 21 (Android 5.0 Lollipop)
- **Compilation Toolchain:** Uni-App Compiler v3 + Gradle 6.x

### Architectural Limitations of Original App
1. **Hybrid WebView Latency:** BLE callbacks from native C++/Java layers were marshaled through JavaScript string bridges, leading to dropped frames, race conditions in notification packet handlers, and UI freeze during packet bursts.
2. **Volatile Local State:** Application state was stored only in Vuex / `uni.setStorageSync`. There was no transactional handshake between UI selection, BLE characteristic write, and controller flash confirmation.
3. **Absence of Background BLE Management:** Background execution was unmanaged; when the OS minimized the WebView, GATT connections abruptly terminated without clean cleanup.

---

## 3. Bluetooth Low Energy (BLE) Stack & Discovery Flow

### BLE Service & Characteristic UUID Architecture
The reference APK interacts with a generic 16-bit UUID profile commonly associated with Chinese e-mobility and two-wheeler motor controllers (Telink / Jerry / JL BLE SoCs):

```
Base Bluetooth UUID: 0000xxxx-0000-1000-8000-00805F9B34FB
```

| Entity | UUID | Permissions / Properties | Purpose |
| :--- | :--- | :--- | :--- |
| **GATT Service** | `0000FFF0-0000-1000-8000-00805F9B34FB` | Primary Service | Scooter Telemetry & Control Profile |
| **Notify Characteristic** | `0000FFF1-0000-1000-8000-00805F9B34FB` | `NOTIFY`, `READ` | Telemetry stream, ACK packets, read-backs |
| **Write Characteristic** | `0000FFF2-0000-1000-8000-00805F9B34FB` | `WRITE`, `WRITE_NO_RESPONSE` | Outbound commands, mode toggles, queries |
| **Descriptor (CCCD)** | `00002902-0000-1000-8000-00805F9B34FB` | `READ`, `WRITE` | Client Characteristic Configuration Descriptor |

### Device Discovery & Connection Workflow
1. **Adapter Init:** Invokes `uni.openBluetoothAdapter()`.
2. **Scan Filter:** Starts discovery via `uni.startBluetoothDevicesDiscovery()`. Filter matched device broadcast names containing prefixes such as `YKZ_`, `YAKUZA`, `E-SCOOTER`, or MAC address prefix matches.
3. **Connection:** Invokes `uni.createBLEConnection()` with MAC address.
4. **Service Discovery:** `uni.getBLEDeviceServices()` searches for `FFF0`.
5. **Characteristic Discovery:** `uni.getBLEDeviceCharacteristics()` finds `FFF1` and `FFF2`.
6. **Descriptor Enablement:** Writes `0x0001` (`ENABLE_NOTIFICATION_VALUE`) to CCCD `0x2902` on `FFF1`.
7. **Write Pipeline:** Outbound packets were transmitted via `FFF2` using `writeType = WRITE_TYPE_NO_RESPONSE` or `WRITE_TYPE_DEFAULT`.

---

## 4. Packet Framing, Checksum & Command Semantics

### Packet Framing Structure
Telemetry and commands follow an 8-byte to 16-byte fixed or framed binary protocol:

```
+------------+------------+------------+---------------+------------+----------+
| Header[0]  | Header[1]  | Opcode/CMD | Payload Data  | Checksum   | Tail     |
| 0x88       | 0x01       | 1 Byte     | N Bytes (1-4) | 1 Byte     | 0xAA (opt)|
+------------+------------+------------+---------------+------------+----------+
```

- **Header Bytes:** `0x88 0x01` (Fixed sync preamble used by the scooter BMS / motor controller).
- **Opcode (Command ID):** Specific 1-byte identifier representing the subsystem register.
- **Payload:** Parameter value (e.g., mode index, integer limit).
- **Checksum Algorithm:** Arithmetic Sum (modulo 256):
  $$\text{Checksum} = \left(\sum_{i=0}^{N-1} \text{Byte}[i]\right) \pmod{256}$$
  Or bitwise XOR sum depending on firmware variant:
  $$\text{XOR Checksum} = \bigoplus_{i=0}^{N-1} \text{Byte}[i]$$

### Observed Command Opcodes
- **`0x3E` — Startup Mode:**
  - Payload `0x00`: Soft Start (सॉफ्ट स्टार्ट)
  - Payload `0x01`: Hard Boot (हार्ड बूट)
- **`0x3A` — Speed Regulation:**
  - Payload `0x00`: Gear 1 (0 / Default)
  - Payload `0x01`: Gear 2 (15 km/h)
  - Payload `0x02`: Gear 3 (25 km/h)
  - Payload `0x03`: Gear 4 (100 km/h)
  *(Note: Original APK had indexing bugs resulting in duplicate payload emission).*
- **`0x42` — Vibration Sensitivity:**
  - Payload `0x01` (Low), `0x02` (Medium), `0x03` (High).
- **`0x45` — Cycling Mode:**
  - Payload `0x00` (Comfort), `0x01` (Cross-country), `0x02` (Cross-country Plus), `0x03` (Racing).
- **`0x51` — Slope Parking (Hill Hold):**
  - Payload `0x01` (Open), `0x00` (Close).

---

## 5. Root Cause Analysis: The "Settings Reset Bug"
The primary user complaint is:
> *"The scooter accepts settings while powered ON and plays an activation tone, but after the scooter is powered OFF and ON again, settings return to their defaults."*

### Deep Technical Analysis
1. **Volatile MCU SRAM vs. Non-Volatile Flash / EEPROM:**
   - In low-cost e-bike/scooter motor controllers (e.g., STM32 clones, Geehy, Jerry, or Telink), runtime parameters reside in volatile SRAM registers.
   - When a BLE command is received, the microcontroller writes the value into SRAM and triggers the speaker/piezo buzzer to emit a positive confirmation tone (`BEEP`).
   - However, writing to internal flash or an external EEPROM requires either:
     a) An explicit **COMMIT / SAVE_TO_FLASH** command opcode (e.g., `0xAA 0x55` or `0xEF`), which the original Uni-App never dispatched; OR
     b) A dedicated firmware commit routine that was omitted by the OEM manufacturer to avoid flash write endurance exhaustion.
2. **Missing Controller Read-Back Verification:**
   - The legacy application had no transaction pipeline. It assumed that receiving a socket write success meant the setting was permanently committed.
   - The app never sent a status query packet (`0x01` Read All) to verify if the controller actually stored the state after power-up.
3. **Mobile UI Illusion:**
   - The original app masked this defect by saving the last selected dropdown index in browser `localStorage`. When the user opened the app, it displayed their old selection even though the physical scooter had reverted to default.

---

## 6. Permissions & Security Audit

### Security Risks in Legacy Application
1. **Unencrypted BLE Transport:**
   - No BLE pairing or bonding (`Just Works` or open GATT). Any nearby phone running a BLE scanner can send `0x88 0x01` packets to modify speed or lock the scooter.
2. **Cleartext Hardcoded Secrets:**
   - Embedded JavaScript bundles contained development server IP addresses and unauthenticated API endpoints.
3. **Over-Permissioning:**
   - Requested unnecessary location and storage permissions without runtime justification.
4. **No Device Identity Pinning:**
   - Connected to any broadcast with a matching name string without verifying unique MAC or cryptographic token.

---

## 7. Migration Recommendations for Native App
1. **Architect in Kotlin with Clean Architecture:** Strict separation between UI (Compose), Domain (Transactions/Use Cases), and Data (BleGattClient, Protocols, DataStore).
2. **Transactional State Machine:** Every setting change must execute as a 4-stage transaction: `VALIDATE` -> `DISPATCH` -> `AWAIT_ACK` -> `READ_BACK_VERIFY`.
3. **Persistence Test Suite:** Add an automated testing tool within the app that guides the user through testing whether a changed setting survives an actual key power-cycle.
4. **Offline-First & Security Guardrails:** No plain HTTP endpoints, contextual permissions, Keystore encryption for secure vehicle tokens, and clear bilingual user alerts.
