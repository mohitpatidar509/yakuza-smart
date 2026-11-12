# Settings Persistence Test Plan & Diagnostics Protocol

## 1. Context & Problem Statement
The user has reported a persistent operational issue with Yakuza electric scooters:
> *"The scooter accepts settings while powered ON and plays an activation tone, but after the scooter is powered OFF and ON again, settings return to their defaults."*

This issue impacts multiple parameters:
- **Startup Mode** (Soft Start vs. Hard Boot)
- **Speed Regulation** (0, 15, 25, 100)
- **Vibration Sensitivity** (Low, Medium, High)
- **Cycling Mode** (Cross-country, Cross-country Plus, Comfort, Racing)

This document outlines the systematic, safe, laboratory-style test plan to isolate whether the issue is caused by:
1. **Volatile SRAM design in scooter controller firmware** (firmware lacks EEPROM/flash retention routines).
2. **Missing Commit / Save Command** (a secondary command like `SAVE_CONFIG` is required after setting modification).
3. **Improper Write Timing or Disconnect Sequencing**.

---

## 2. Safety Guidelines (सुरक्षा निर्देश)

> [!CAUTION]
> **DO NOT TEST WHILE RIDING.**
> Testing must ONLY be conducted on a stationary scooter with the motor disengaged.

### Safety Checklist:
1. Put the scooter on its **dual center stand (मेन स्टैंड)** so that the rear drive wheel is elevated above the ground.
2. Ensure the throttle is untouched and the brake lock is engaged if present.
3. Conduct the test in an open, well-ventilated area away from traffic.
4. Have the physical ignition key or master NFC card readily accessible.

---

## 3. Step-by-Step Test Procedure (चरणबद्ध परीक्षण विधि)

### Phase 1: Baseline Read (मूल स्थिति का रिकॉर्ड)
1. Power ON the scooter via physical key switch.
2. Open the **Yakuza Smart** app on Android.
3. Navigate to **Diagnostics > Persistence Test**.
4. Tap **Connect & Read Baseline**.
5. The app queries the controller using `0x88 0x01 0x04 0x01 ...` and displays current hardware registers:
   - Baseline Startup Mode
   - Baseline Speed Limit
   - Baseline Vibration Sensitivity
   - Baseline Cycling Mode

### Phase 2: Single Parameter Modification (एक सेटिंग बदलना)
1. Select **Startup Mode**.
2. If currently `Soft Start (0x00)`, select `Hard Boot (0x01)`.
3. Tap **Transmit & Verify Immediate Response**.
4. Listen for scooter acoustic feedback:
   - Did the controller emit a chime/beep? (हाँ / नहीं)
5. Check app screen: Did the controller return a verified ACK packet matching opcode `0x3E`?
6. The app marks the immediate state: `IMMEDIATE_WRITE_SUCCESS`.

### Phase 3: Controlled Power-Cycle (पावर बंद और चालू करना)
1. **Turn OFF** the scooter ignition switch (चाबी बंद करें).
2. **Wait 15 Seconds:** Allow power supply capacitors on the controller board to completely discharge (कैपेसिटर डिस्चार्ज होने का 15 सेकंड इंतजार करें).
3. **Turn ON** the scooter ignition switch (चाबी पुनः चालू करें).

### Phase 4: Reconnection & Read-Back (पुनः कनेक्ट करके जांचना)
1. In the app, tap **Reconnect & Verify Persistence**.
2. The app reconnects BLE, discovers characteristics, and immediately requests fresh telemetry without using cached local values.
3. Compare the read value with the modified value:
   - **Case A (Persisted):** Read value = `Hard Boot (0x01)`. Result: `PASS (Controller supports Flash persistence)`.
   - **Case B (Reverted):** Read value = `Soft Start (0x00)`. Result: `FAIL (Reverted to RAM default)`.

---

## 4. Test Matrix & Log Table

| Test # | Parameter Tested | Value Written | Immediate Beep? | Immediate Read-back | Post Power-Cycle Read | Result (PERSISTED / REVERTED) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **P-01** | Startup Mode | Hard Boot (`0x01`) | Yes / No | Verified | Soft Start (`0x00`) | Pending Hardware Test |
| **P-02** | Startup Mode | Soft Start (`0x00`) | Yes / No | Verified | Default | Pending Hardware Test |
| **P-03** | Speed Limit | 25 km/h (`0x02`) | Yes / No | Verified | Default | Pending Hardware Test |
| **P-04** | Vibration | High (`0x03`) | Yes / No | Verified | Medium (`0x02`) | Pending Hardware Test |
| **P-05** | Cycling Mode | Racing (`0x03`) | Yes / No | Verified | Comfort (`0x00`) | Pending Hardware Test |

---

## 5. Potential Fixes Based on Findings

### Scenario 1: Controller Reverts All Settings (Volatile RAM Architecture)
- **Technical Explanation:** The manufacturer programmed the microcontroller to keep runtime configuration only in volatile RAM registers. No flash write instruction exists in the stock firmware.
- **App-Side Solution:** The smartphone cannot force physical microcontroller hardware to write to unmapped flash memory. However, the app can offer a **"Sync on Connect" (कनेक्ट होते ही लागू करें)** toggle that allows the user, upon intentional connection, to re-apply their desired preferences with a single tap or automatic handshake.

### Scenario 2: Missing Explicit Save/Commit Command
- **Technical Explanation:** The controller requires an explicit `COMMIT` packet (e.g. `0x88 0x01 0x04 0xEE 0x55 0x...`) after writing parameters.
- **App-Side Solution:** The app implements a trial "Save to EEPROM" payload dispatch during persistence testing to evaluate if a flash write cycle can be unlocked.

---

## 6. Bilingual User Instructions (सरल हिंदी निर्देश)

1. **स्कूटर को मेन स्टैंड पर लगाएं:** परीक्षण के दौरान पिछला पहिया जमीन से ऊपर होना चाहिए।
2. **ऐप में Persistence Test खोलें:** 'Diagnostics' मेनू में जाकर 'Persistence Test' चुनें।
3. **एक सेटिंग बदलें:** उदाहरण के लिए Startup Mode को 'Hard Boot' पर सेट करें।
4. **बीप की आवाज़ सुनें:** स्कूटर से बीप की आवाज़ आने का मतलब है कि उसने कमांड स्वीकार कर लिया है।
5. **स्कूटर की चाबी बंद करें:** चाबी बंद करें और 15 सेकंड तक रुकें ताकि कंट्रोलर पूरी तरह रीसेट हो जाए।
6. **चाबी दोबारा चालू करें:** स्कूटर चालू करके ऐप में 'Verify' बटन दबाएं।
7. **परिणाम देखें:** यदि सेटिंग वही रहती है, तो कंट्रोलर में मेमोरी सेविंग काम कर रही है। यदि पुरानी सेटिंग वापस आ जाती है, तो यह कंट्रोलर के फर्मवेयर की सीमा है।
