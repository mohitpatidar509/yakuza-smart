package com.yakuzasmart.app.core.localization

object AppStrings {
    fun appTitle(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Yakuza Smart"
        Language.HINDI -> "याकुज़ा स्मार्ट"
    }

    fun tagline(lang: Language): String = when (lang) {
        Language.ENGLISH -> "SMART ELECTRIC MOBILITY"
        Language.HINDI -> "स्मार्ट इलेक्ट्रिक मोबिलिटी"
    }

    fun notConnected(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Not Connected"
        Language.HINDI -> "कनेक्ट नहीं है"
    }

    fun connected(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Connected"
        Language.HINDI -> "कनेक्टेड"
    }

    fun simulated(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Simulated Controller"
        Language.HINDI -> "सिम्युलेटेड कंट्रोलर"
    }

    fun scan(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Scan for Scooter"
        Language.HINDI -> "स्कूटर स्कैन करें"
    }

    fun disconnect(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Disconnect"
        Language.HINDI -> "डिस्कनेक्ट करें"
    }

    fun dashboard(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Dashboard"
        Language.HINDI -> "डैशबोर्ड"
    }

    fun vehicleSettings(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Vehicle Settings"
        Language.HINDI -> "वाहन सेटिंग्स"
    }

    fun diagnostics(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Diagnostics & Logs"
        Language.HINDI -> "डायग्नोस्टिक्स व लॉग"
    }

    fun persistenceTest(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Persistence Test"
        Language.HINDI -> "पर्सिस्टेंस टेस्ट"
    }

    fun appSettings(lang: Language): String = when (lang) {
        Language.ENGLISH -> "App Settings"
        Language.HINDI -> "ऐप सेटिंग्स"
    }

    fun nfcManagement(lang: Language): String = when (lang) {
        Language.ENGLISH -> "NFC Card Management"
        Language.HINDI -> "NFC कार्ड प्रबंधन"
    }

    fun vehicleInfo(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Vehicle Information"
        Language.HINDI -> "वाहन जानकारी"
    }

    fun speed(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Speed"
        Language.HINDI -> "गति (स्पीड)"
    }

    fun battery(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Battery"
        Language.HINDI -> "बैटरी"
    }

    fun estimatedRange(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Est. Range"
        Language.HINDI -> "अनुमानित रेंज"
    }

    fun totalMileage(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Total Mileage"
        Language.HINDI -> "कुल दूरी (ओडो)"
    }

    fun batteryVoltage(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Battery Voltage"
        Language.HINDI -> "बैटरी वोल्टेज"
    }

    fun voltage(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Voltage"
        Language.HINDI -> "वोल्टेज"
    }

    fun motorCurrent(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Motor Current"
        Language.HINDI -> "मोटर करंट"
    }

    fun controllerTemp(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Controller Temp"
        Language.HINDI -> "कंट्रोलर तापमान"
    }

    // Setting Titles & Descriptions
    fun startupMode(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Startup Mode"
        Language.HINDI -> "स्टार्टअप मोड"
    }

    fun startupModeDesc(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Select acceleration ramp rate when launching from zero speed."
        Language.HINDI -> "शून्य गति से शुरू करते समय त्वरण दर (एक्सेलेरेशन) चुनें।"
    }

    fun speedRegulation(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Speed Regulation"
        Language.HINDI -> "स्पीड रेगुलेशन"
    }

    fun speedRegulationDesc(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Select powertrain speed limiting or power gear tier."
        Language.HINDI -> "अधिकतम गति सीमा अथवा पावर गियर स्तर चुनें।"
    }

    fun vibrationSensitivity(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Vibration Sensitivity"
        Language.HINDI -> "वाइब्रेशन सेंसिटिविटी"
    }

    fun vibrationSensitivityDesc(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Anti-theft motion detection trigger threshold."
        Language.HINDI -> "चोरी रोधी अलार्म के लिए मोशन सेंसर संवेदनशीलता।"
    }

    fun cyclingMode(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Cycling Mode"
        Language.HINDI -> "साइक्लिंग मोड"
    }

    fun cyclingModeDesc(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Motor torque curve and regenerative braking profile."
        Language.HINDI -> "मोटर टॉर्क कर्व और रीजेनरेटिव ब्रेकिंग प्रोफाइल।"
    }

    fun slopeParking(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Slope Parking (Hill Hold)"
        Language.HINDI -> "स्लोप पार्किंग (हिल होल्ड)"
    }

    fun smartFeatures(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Smart Features"
        Language.HINDI -> "स्मार्ट फीचर्स"
    }

    fun safetyFeatures(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Safety Features (PCS/TCS/ABS)"
        Language.HINDI -> "सुरक्षा सेटिंग्स (PCS/TCS/ABS)"
    }

    fun soundSettings(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Sound & Indicators"
        Language.HINDI -> "ध्वनि एवं इंडिकेटर"
    }

    // Options
    fun hardBoot(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Hard Boot"
        Language.HINDI -> "हार्ड बूट (तेज)"
    }

    fun softStart(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Soft Start"
        Language.HINDI -> "सॉफ्ट स्टार्ट (आरामदेह)"
    }

    fun low(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Low"
        Language.HINDI -> "कम"
    }

    fun medium(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Medium"
        Language.HINDI -> "मध्यम"
    }

    fun high(lang: Language): String = when (lang) {
        Language.ENGLISH -> "High"
        Language.HINDI -> "ज्यादा"
    }

    fun crossCountry(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Cross-country"
        Language.HINDI -> "क्रॉस-कंट्री"
    }

    fun crossCountryPlus(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Cross-country Plus"
        Language.HINDI -> "क्रॉस-कंट्री प्लस"
    }

    fun comfort(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Comfort"
        Language.HINDI -> "कम्फर्ट"
    }

    fun racing(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Racing"
        Language.HINDI -> "रेसिंग"
    }

    fun open(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Open (Enabled)"
        Language.HINDI -> "चालू (एक्टिव)"
    }

    fun close(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Close (Disabled)"
        Language.HINDI -> "बंद (निष्क्रिय)"
    }

    // Warning & Safety
    fun stationarySafetyWarning(lang: Language): String = when (lang) {
        Language.ENGLISH -> "CAUTION: Test on dual center stand only. Rear wheel must be off ground. Never change settings while riding."
        Language.HINDI -> "सावधानी: केवल डबल मेन स्टैंड पर परीक्षण करें। पिछला पहिया हवा में होना चाहिए। राइडिंग के दौरान सेटिंग न बदलें।"
    }

    fun persistenceResetNotice(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Controller Persistence Note: Some scooter controllers do not save settings to flash memory when powered off. Use 'Persistence Test' to verify your controller."
        Language.HINDI -> "कंट्रोलर मेमोरी सूचना: कुछ स्कूटर कंट्रोलर पावर बंद होने पर फ्लैश मेमोरी में सेटिंग सेव नहीं करते। अपने कंट्रोलर की जांच के लिए 'पर्सिस्टेंस टेस्ट' चलाएं।"
    }

    fun unverifiedHardwareBadge(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Hardware Verification Pending"
        Language.HINDI -> "हार्डवेयर सत्यापन लंबित"
    }

    fun verifiedBadge(lang: Language): String = when (lang) {
        Language.ENGLISH -> "Verified by Controller"
        Language.HINDI -> "कंट्रोलर द्वारा सत्यापित"
    }
}
