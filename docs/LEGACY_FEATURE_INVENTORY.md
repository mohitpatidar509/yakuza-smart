# Legacy Feature Inventory — Yakuza Smart

## 1. Executive Summary
This document compiles the exhaustive catalogue of all vehicle settings, dashboard features, telemetry indicators, and operational options identified from the original Yakuza electric scooter companion application (`Yakuza.apk`), user-provided screenshots, and project conversations.

Each feature is categorized, mapped with English and Hindi labels, correlated with its preliminary BLE control payload, and evaluated for hardware verification status.

---

## 2. Comprehensive Settings Catalogue

### Group A: Startup Mode (स्टार्टअप मोड)
*Controls the throttle acceleration response when taking off from stationary.*
- **Legacy UI Options:**
  1. **Hard Boot (हार्ड बूट)**: Immediate throttle response and maximum torque upon engagement.
  2. **Soft Start (सॉफ्ट स्टार्ट)**: Gradual power ramp-up to prevent wheel spin and sudden jerks.
- **Preliminary Protocol Mapping:** Opcode `0x3E`. Payload `0x00` (Soft Start), `0x01` (Hard Boot).
- **Verification Status:** Preliminary static analysis. Requires read-back confirmation.

### Group B: Speed Regulation (स्पीड रेगुलेशन)
*Speed limiter or gear selection tiers.*
- **Legacy UI Options:**
  - `0` (Unrestricted / Neutral / Default)
  - `15` (15 km/h low-speed commuter mode)
  - `25` (25 km/h standard regulatory limit)
  - `100` (100% full-throttle unlock mode)
- **Note on Units:** Preserved as labeled in legacy UI. Do not assume arbitrary km/h without controller telemetry confirmation.
- **Legacy Anomaly:** The original Uni-App exhibited duplicate index mapping where selecting gear 3 or 4 yielded identical payload bytes.
- **Verification Status:** Unverified controller register.

### Group C: Vibration Sensitivity (वाइब्रेशन सेंसिटिविटी)
*Sensitivity threshold for the on-board anti-theft vibration sensor / tilt alarm.*
- **Legacy UI Options:**
  1. **Low (कम)**: Requires hard impact or prolonged displacement to trigger alarm.
  2. **Medium (मध्यम)**: Balanced sensitivity for normal street parking.
  3. **High (ज्यादा)**: Triggers siren on subtle motion, touching, or wind shock.
- **Verification Status:** Legacy UI confirmed; controller persistence unverified.

### Group D: Cycling Mode (साइक्लिंग मोड)
*Vehicle powertrain dynamic profiles (throttle curves, regen braking, field weakening).*
- **Legacy UI Options:**
  1. **Cross-country (क्रॉस-कंट्री)**: Off-road high torque response.
  2. **Cross-country Plus (क्रॉस-कंट्री प्लस)**: Maximum power output with aggressive regenerative braking.
  3. **Comfort (कम्फर्ट)**: Linear power delivery optimized for range and passenger comfort.
  4. **Racing (रेसिंग)**: High RPM motor timing for maximum top speed.
- **Verification Status:** Legacy UI confirmed; incomplete opcode mapping in original APK.

### Group E: Slope Parking / Hill Hold (स्लोप पार्किंग)
*Electronic motor brake holding on gradients to prevent rolling backwards.*
- **Legacy UI Options:**
  1. **Open (चालू / एक्टिव)**: Motor lock engages upon zero-speed detection on an incline.
  2. **Close (बंद / निष्क्रिय)**: Free wheeling; mechanical brake required.
- **Verification Status:** Legacy UI confirmed; safety-critical verification pending.

### Group F: Smart Features (स्मार्ट फीचर्स)
- **1. Smart Helmet (स्मार्ट हेलमेट):** Wireless interlock requiring verified helmet beacon before enabling motor drive (On / Off).
- **2. Password Unlock (पासवर्ड अनलॉक):** Vehicle lockout requiring PIN entry via app or meter keypad to start.
- **3. Child Mode (चाइल्ड मोड):** Locks speed to minimum safety threshold (<10 km/h) and limits peak torque.
- **4. Welcome Mode (वेलकम मोड):** Courtesy LED animation and audio greeting when BLE auto-connects.
- **5. Quiet Mode (क्वाइट मोड):** Mutes all system chimes, confirmation beeps, and startup melodies.
- **Verification Status:** Retained in UI catalogue; marked "Controller Verification Pending".

### Group G: Safety Features (सुरक्षा सेटिंग्स)
- **PCS (Pre-Collision System / Proximity Alert):** Radar/ultrasonic sensor warnings or throttle cutoff.
- **TCS (Traction Control System):** Limits wheel slip on wet/slick asphalt.
- **ABS (Electronic Anti-lock Braking System):** Modulates regenerative motor counter-EMF to prevent wheel lockup.
- **Verification Status:** Displayed as "Vehicle-supported setting (सत्यापन लंबित)".

### Group H: Sound and Indicators (ध्वनि एवं इंडिकेटर)
- **Turn Signal Tone (इंडिकेटर टोन):** Audible relay beeping when turn signals are active (On / Off).
- **Voice Settings (वॉइस सेटिंग्स):** Synthesized voice warnings (e.g., "Helmet on", "Over-speed", "Low battery").
- **Verification Status:** Retained in UI; hardware register mapping pending.

### Group I: NFC Card Management (NFC कार्ड प्रबंधन)
*RFID / NFC key fob management for digital ignition.*
- **1. Read Card (कार्ड पढ़ें):** Prompts controller into card-enrollment mode.
- **2. Clear Card (कार्ड हटाएं):** Erases registered master and guest NFC key tags.
- **Verification Status:** Retained with strict confirmation dialogs to prevent accidental key clearance.

### Group J: Other Vehicle Options (अन्य वाहन विकल्प)
- **Voltage Settings (वोल्टेज सेटिंग्स):** 48V / 60V / 72V system battery configuration monitoring.
- **Induction Mode (इंडक्शन मोड):** Keyless proximity lock/unlock based on BLE RSSI beacon distance.
- **Distance / Proximity Sensitivity:** Near (पास), Middle (मध्यम), Far (दूर).
- **Verification Status:** Retained in UI catalogue.

---

## 3. Dashboard Telemetry & Status Indicators
The legacy application contained placeholders for telemetry which were frequently either frozen or simulated. The new Yakuza Smart app strictly categorizes these into verified vs. unavailable:

| Metric | Legacy Behavior | Native App Rule |
| :--- | :--- | :--- |
| **Battery Percentage** | Hardcoded or unvalidated byte | Shown ONLY when valid packet received; labeled with source |
| **Speed (km/h)** | Often laggy or frozen at 0 | Real-time gauge; dashes (`--`) when disconnected |
| **Estimated Range** | Naive multiplier on battery % | Calculated based on controller voltage telemetry or hidden if unsupported |
| **Vehicle Faults** | Static icon | Dynamic diagnostics bar (Brake Fault, Throttle Fault, Motor Hall, Controller Over-temp) |
| **Connection State** | Boolean | Multi-state: Disconnected, Scanning, Connecting, Authenticating, Ready, Syncing |
| **RSSI Signal Bar** | Missing | Real-time dBm indicator with visual signal bars |

---

## 4. Policy for Unsupported or Pending Features
1. **No Silent Removal:** No feature from the original app is removed.
2. **Explicit Verification Badges:** Every setting card displays a badge:
   - `VERIFIED`: Confirmed by controller acknowledgement.
   - `PENDING_SYNC`: Sent to controller, waiting for read-back verification.
   - `SIMULATED`: Running under built-in BLE simulator.
   - `UNVERIFIED_HARDWARE`: Legacy option present in UI, but controller firmware support is unverified.
3. **Safety Protection:** High-risk actions (NFC clear, ABS toggle, Voltage alteration) require secondary confirmation.
