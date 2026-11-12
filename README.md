# Yakuza Smart — Official Native Android Companion App

A modern, production-grade native Android application for **Yakuza Electric Scooters**, developed in **Kotlin** and **Jetpack Compose** following **MVVM Clean Architecture**.

This project completely redevelops and replaces the legacy DCloud Uni-App hybrid wrapper with a high-performance, robust, and secure native Android BLE companion.

---

## 1. Key Features

- **Premium Black & Orange Mobility Theme:** Modern charcoal/black background (`#0C0D10`), vibrant orange accents (`#FF6A00`), accessible contrast, and smooth transitions.
- **Bilingual Interface:** Instant runtime switching between **English** and **Hindi (हिन्दी)** with persistent language preferences.
- **20 Full-Featured Screens:** Complete catalogue of all legacy vehicle settings, dashboard telemetry, diagnostics, and NFC management.
- **Verified Setting Transactions:** Strict 4-stage transaction pipeline (`VALIDATE` -> `DISPATCH` -> `AWAIT_ACK` -> `READ_BACK_VERIFY`) to solve the settings reset defect.
- **Built-in BLE Simulator:** Full in-memory mock scooter controller with telemetry streaming (1 Hz) and volatile SRAM power-cycle emulation for hardware-free development and testing.
- **Android Security & Least Privilege:** Adheres to Android 12+ (API 31+) companion standards using `neverForLocation` flag. Zero location tracking, zero external cloud dependencies.
- **Velocity Gating & Safety Interlocks:** Parameter modification is programmatically blocked when telemetry speed $> 0.0\text{ km/h}$.

---

## 2. All 20 Screens Catalogue

1. `SplashScreen` — Premium Yakuza branding with animated launcher.
2. `OnboardingScreen` — 3-point feature introduction with language selector.
3. `BluetoothPermissionScreen` — Contextual Android 12+ / legacy permission explainer with privacy badge.
4. `DeviceScanScreen` — Active BLE scan list with signal RSSI bars and Yakuza brand detection.
5. `DeviceConnectionScreen` — Interactive GATT handshake, service discovery (0xFFF0), and CCCD notification setup.
6. `DashboardScreen` — Live telemetry (Speed gauge, Battery %, Range, Voltage, Current, Temp, Faults, Quick Actions).
7. `AllVehicleSettingsScreen` — Categorized settings menu with live sync badges.
8. `StartupModeScreen` — Detailed selector for Hard Boot vs. Soft Start with live read-back verification.
9. `SpeedRegulationScreen` — Speed limiter gear selection (0, 15, 25, 100) with stationary interlocks.
10. `VibrationSensitivityScreen` — Low, Medium, High alarm sensitivity thresholds.
11. `CyclingModeScreen` — Dynamic powertrain curves (Cross-country, Comfort, Racing).
12. `SafetySettingsScreen` — Slope Parking (Hill Hold), PCS, TCS, and ABS toggle panel.
13. `SmartFeaturesScreen` — Helmet Interlock, Password PIN, Child Mode, Welcome Lights, Quiet Mode.
14. `SoundSettingsScreen` — Turn Signal Chimes and Voice prompt controls.
15. `NfcManagementScreen` — Secure Read Card & Clear Card with safety dialogs.
16. `VehicleInfoScreen` — Battery voltage auto-detect, induction proximity mode, hardware MAC, firmware specs.
17. `DiagnosticsScreen` — Live packet monitor (TX/RX hex dump), log export, and Persistence Test Suite.
18. `AppSettingsScreen` — Simulator toggle, cache clear, sync on connect settings.
19. `LanguageSelectionScreen` — Seamless runtime switch between English and Hindi.
20. `AboutScreen` — Architecture specs, safety guidelines, and offline privacy policy.

---

## 3. Architecture & Project Structure

```
app/src/main/java/com/yakuzasmart/app/
├── core/
│   ├── constants/       # BleConstants (UUIDs, Opcodes, Timeouts)
│   ├── localization/    # Language, AppStrings (English & Hindi)
│   ├── logging/         # DiagnosticLogger (In-memory packet logs & export)
│   ├── security/        # SafetyInterlocks (Velocity gating)
│   └── theme/           # Color, Type, Theme (Black & Orange)
├── data/
│   ├── bluetooth/       # BleManager, BleProtocolHandler, BleSimulator
│   ├── local/           # AppPreferences (DataStore), SettingsCache
│   └── repository/      # ScooterRepositoryImpl, SettingsRepositoryImpl
├── domain/
│   ├── model/           # ScooterState, ScooterSetting, SettingTransaction, PersistenceTestResult
│   ├── repository/      # ScooterRepository, SettingsRepository
│   └── usecase/         # ScanDevicesUseCase, ConnectScooterUseCase, UpdateSettingUseCase
└── presentation/
    ├── components/      # YakuzaTopBar, TelemetryCard, StatusBadge, SettingCard, YakuzaButton
    ├── navigation/      # Screen, NavGraph (Jetpack Navigation)
    ├── screens/         # All 20 UI screens
    └── viewmodel/       # ScooterViewModel
```

---

## 4. BLE Protocol Summary

- **Service UUID:** `0000FFF0-0000-1000-8000-00805F9B34FB`
- **Notify Characteristic:** `0000FFF1-0000-1000-8000-00805F9B34FB`
- **Write Characteristic:** `0000FFF2-0000-1000-8000-00805F9B34FB`
- **Frame Preamble:** `0x88 0x01`
- **Framing:** `[0x88, 0x01, LEN, OPCODE, ...PAYLOAD, CHECKSUM]`
- **Checksum:** Modulo-256 arithmetic sum `(Sum of all bytes 0..N-2) & 0xFF`
- **Key Opcodes:**
  - `0x01`: Query Status / Telemetry Stream
  - `0x3E`: Startup Mode (`0x00` = Soft Start, `0x01` = Hard Boot)
  - `0x3A`: Speed Regulation (`0x00`, `0x01`, `0x02`, `0x03`)
  - `0x42`: Vibration Sensitivity (`0x01` = Low, `0x02` = Med, `0x03` = High)
  - `0x45`: Cycling Mode (`0x00` = Comfort, `0x01` = Cross, `0x02` = Cross+, `0x03` = Race)
  - `0x51`: Slope Parking (`0x00` = Close, `0x01` = Open)

---

## 5. Build & Setup Instructions

### Prerequisites
- **Android Studio:** Hedgehog (2023.1.1), Iguana, Jellyfish, Koala, or Ladybug.
- **JDK:** OpenJDK 17 or higher.
- **Android SDK:** Compile SDK 35, Min SDK 26 (Android 8.0 Oreo).

### How to Build Debug APK
1. Open Android Studio.
2. Select **Open an Existing Project** and navigate to `YakuzaSmart_Android_Project`.
3. Allow Gradle to sync.
4. Run:
   ```bash
   ./gradlew assembleDebug
   ```
   Or select **Build > Build Bundle(s) / APK(s) > Build APK(s)** in Android Studio.
5. The debug APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 6. Real Scooter Testing Instructions (हिन्दी में निर्देश)

> [!CAUTION]
> **सुरक्षा चेतावनी:** परीक्षण के दौरान कभी भी चलती हुई गाड़ी पर सेटिंग न बदलें।

1. **स्कूटर को मेन स्टैंड पर लगाएं:** स्कूटर को डबल मेन स्टैंड पर खड़ा करें ताकि पिछला पहिया जमीन से ऊपर रहे।
2. **चाबी चालू करें:** स्कूटर की चाबी चालू करें।
3. **ऐप खोलें व कनेक्ट करें:** Yakuza Smart ऐप खोलकर 'Scan for Scooter' दबाएं और अपने स्कूटर से कनेक्ट करें।
4. **'Persistence Test' स्क्रीन खोलें:** Diagnostics मेनू में जाकर 'Persistence Test' चुनें।
5. **एक सेटिंग बदलें:** Startup Mode को 'Hard Boot' पर सेट करें और स्कूटर से आने वाली बीप की आवाज़ सुनें।
6. **चाबी बंद करें (15 सेकंड प्रतीक्षा):** स्कूटर की चाबी बंद करें और 15 सेकंड रुकें ताकि कंट्रोलर के कैपेसिटर डिस्चार्ज हो जाएं।
7. **चाबी चालू करें व Verify दबाएं:** चाबी चालू करके ऐप में 'Evaluate Persistence' दबाएं।
8. **परिणाम का विश्लेषण:**
   - यदि सेटिंग Hard Boot बनी रहती है -> कंट्रोलर में फ्लैश मेमोरी सेविंग समर्थित है।
   - यदि सेटिंग Soft Start पर रीसेट हो जाती है -> यह कंट्रोलर के फर्मवेयर की वोलेटाइल RAM सीमा है। ऐप में 'Sync on Connect' विकल्प का उपयोग करें ताकि कनेक्ट होते ही आपकी सेटिंग्स स्वतः लागू हो जाएं।
