# Known Limitations & Hardware Firmware Report — Yakuza Smart

## 1. Executive Summary
This document records known technical constraints, hardware firmware limitations, and protocol edge cases identified during the development of the Yakuza Smart native application.

---

## 2. The Settings Persistence Limitation (The "Reset Bug")

### Root Cause
1. **Volatile SRAM Storage in Motor Controller:**
   The electric scooter motor controller uses a microcontroller (MCU) where runtime configuration registers reside in volatile Static RAM (SRAM).
   - When a user changes a setting via BLE while the scooter is ON, the MCU writes the value into SRAM and sounds a confirmation beep.
   - However, the MCU does NOT automatically write these values into non-volatile Flash memory or an external EEPROM.
2. **Missing Controller Flash-Commit Routine:**
   - Either the OEM manufacturer omitted an EEPROM chip to reduce bill-of-materials cost; OR
   - The controller firmware requires a specific, secondary **COMMIT / FLASH_SAVE** packet sequence (such as Opcode `0xEE 0x55 0xAA`) which the original companion app never implemented.
3. **App Architecture Reality:**
   A smartphone mobile application cannot physically write to a scooter's non-volatile memory if the controller firmware does not implement flash persistence.
   - The new Yakuza Smart app provides an explicit **"Sync on Connect"** workflow to re-apply the user's preferred settings each time the scooter is powered on and connected, mitigating this hardware limitation cleanly.

---

## 3. Speed Regulation Option Interpretation

- In the original application, options were labeled: `0`, `15`, `25`, `100`.
- **Engineering Note:** These numbers do not necessarily correspond to exact speedometers limits in kilometers per hour:
  - `0`: Unrestricted Default / Neutral Gear
  - `15`: Low-speed Commuter Gear (approx. 15 km/h)
  - `25`: Standard Regulatory Limit (25 km/h for Indian low-speed EV rules)
  - `100`: High-speed / 100% full-duty PWM unlock
- The new app preserves original labels while avoiding fabricated units until physical dynamometer or road telemetry calibration is completed.

---

## 4. Android OS & Vendor Battery Optimizations

- Aggressive Android vendor battery management (such as Xiaomi MIUI/HyperOS, OnePlus OxygenOS, Vivo FuntouchOS) terminates background BLE scanning after prolonged inactivity.
- **Mitigation:** Yakuza Smart avoids indefinite scanning, utilizes targeted scan timeouts (15 seconds), and gracefully reconnects using the cached device MAC address upon user initiation.
