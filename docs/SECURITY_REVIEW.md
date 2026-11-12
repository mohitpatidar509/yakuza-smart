# Security Review & Threat Model — Yakuza Smart

## 1. Executive Summary
This document provides a comprehensive security review and threat model for the Yakuza Smart native Android application. It establishes architectural security controls, permission hygiene, Bluetooth transport protections, local data cryptography, and vehicle safety interlocks.

---

## 2. Threat Modeling & Attack Surfaces

| Attack Vector | Threat Level | Vulnerability in Legacy App | Native Android Mitigation |
| :--- | :--- | :--- | :--- |
| **Unauthenticated BLE Injection** | HIGH | Any nearby BLE device could send `0x88 0x01` frames to alter speed or engage hill parking. | Proximity RSSI validation (> -70 dBm for pairing), persistent device MAC binding, optional PIN authentication handshake. |
| **Motor Cutoff While Moving** | CRITICAL | Inadvertent dispatch of lockout, slope parking, or child mode while the scooter was moving at high speed. | **Firmware Interlock Enforcement:** App strictly prohibits sending motor-lock, park, or diagnostic commands when telemetry speed is > 0 km/h. |
| **Cleartext Telemetry Interception** | MEDIUM | Telemetry broadcasted in unencrypted GATT characteristics. | Validated frame structure, payload boundary checks, sanitize logs before export to prevent leakage of user locations or travel history. |
| **Stale Data / Man-in-the-Middle** | MEDIUM | App displayed outdated local cache as live vehicle status. | Strict timestamping, transactional states (`PENDING`, `VERIFIED`, `STALE`), visible disconnection indicators. |
| **Over-Permission Exploitation** | LOW | Requested broad background location permissions without explanation. | Adopt Android 12+ companion device permissions with `neverForLocation` flag; no location required on modern OS. |

---

## 3. Permission Architecture

The application adopts the principle of least privilege, adhering strictly to Android 12 (API 31) and Android 13+ permission standards:

```xml
<!-- Legacy BLE (Android 6.0 - 11, API 23-30) -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />

<!-- Modern BLE (Android 12+, API 31+) -->
<uses-permission 
    android:name="android.permission.BLUETOOTH_SCAN" 
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Hardware feature declaration -->
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

### Key Security Decisions:
1. **`neverForLocation` Flag:** We explicitly declare `neverForLocation` on `BLUETOOTH_SCAN`. This informs Android that the app uses BLE exclusively to communicate with the scooter hardware, completely eliminating the requirement for runtime location tracking on Android 12+.
2. **Contextual Permission Flow:** The app never prompts permissions on initial launch. Instead, a dedicated `BluetoothPermissionScreen` explains why Bluetooth is required, shown only when the user taps "Scan for Scooter".

---

## 4. Vehicle Safety Guardrails & Interlocks

To prevent physical harm or equipment damage, the software implements programmatic safety interlocks:

1. **Velocity Gating:**
   - Command dispatch for critical settings (Slope Parking, Motor Lock, Child Mode, Speed Limit change) is **blocked** if the latest telemetry reports speed $> 0.0\text{ km/h}$.
   - The UI displays an explicit warning: *"Cannot modify setting while vehicle is in motion."*
2. **Double Confirmation on Destructive Actions:**
   - Clearing NFC keys (`Clear Card`) requires a two-step dialog with an explicit confirmation warning: *"All registered NFC keys will be deleted. You will need a master key to re-program."*
3. **Range & Sanity Checks:**
   - Outbound packet payloads are validated against strict enum boundaries before serialization. Undefined opcodes or out-of-range integer values are rejected at the domain layer.

---

## 5. Local Storage & Cryptographic Hygiene

1. **Jetpack DataStore Preferences:**
   - Used for application preferences, UI language preference, and last-known device MAC address.
2. **Android Keystore System:**
   - When storing sensitive vehicle unlock tokens or PINs, cryptographic keys are generated in hardware-backed Android Keystore (`KeyGenParameterSpec` with `PURPOSE_ENCRYPT | PURPOSE_DECRYPT`, AES-256 GCM).
3. **Diagnostic Log Sanitization:**
   - In-app diagnostic logs record raw packet hex sequences, timestamps, and error codes for debugging, but never capture user personal information, GPS coordinates, or credentials.
4. **Offline Resilience:**
   - The application does NOT rely on external cloud servers or unencrypted HTTP APIs to function. All scooter management is handled locally through direct BLE communication.
