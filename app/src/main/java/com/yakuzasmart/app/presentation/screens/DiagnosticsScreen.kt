package com.yakuzasmart.app.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.logging.DiagnosticLogEntry
import com.yakuzasmart.app.core.logging.DiagnosticLogger
import com.yakuzasmart.app.core.logging.LogLevel
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.*
import com.yakuzasmart.app.presentation.components.SafetyWarningBanner
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun DiagnosticsScreen(
    state: ScooterState,
    persistenceRecords: List<PersistenceTestRecord>,
    language: Language,
    onLanguageToggle: () -> Unit,
    onTriggerSimulatedPowerCycle: () -> Unit,
    onUpdateSetting: (SettingCategory, SettingOption) -> Unit,
    onRecordBaseline: (SettingCategory, SettingOption?) -> Unit,
    onRecordWritten: (SettingCategory, SettingOption, Boolean, Boolean) -> Unit,
    onVerifyPostPowerCycle: (SettingCategory, SettingOption?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val logs by DiagnosticLogger.logs.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Persistence Test, 1: Packet Stream
    var testStep by remember { mutableStateOf(PersistenceTestStep.STEP_1_RECORD_BASELINE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.diagnostics(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) YakuzaOrangePrimary else YakuzaDarkCharcoal,
                    contentColor = if (selectedTab == 0) androidx.compose.ui.graphics.Color.White else YakuzaTextSecondary
                )
            ) {
                Text(if (language == Language.HINDI) "पर्सिस्टेंस टेस्ट" else "Persistence Test", fontSize = 12.sp)
            }

            Button(
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) YakuzaOrangePrimary else YakuzaDarkCharcoal,
                    contentColor = if (selectedTab == 1) androidx.compose.ui.graphics.Color.White else YakuzaTextSecondary
                )
            ) {
                Text(if (language == Language.HINDI) "पैकेट मॉनिटर" else "Live Packets", fontSize = 12.sp)
            }
        }

        if (selectedTab == 0) {
            // Persistence Test Workflow UI
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    SafetyWarningBanner(language = language)
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = if (language == Language.HINDI)
                                    "कंट्रोलर मेमोरी रीसेट परीक्षण (Persistence Workflow)"
                                else
                                    "Power-Cycle Settings Persistence Test",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = YakuzaOrangePrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (language == Language.HINDI)
                                    "यह टूल परीक्षण करता है कि क्या कंट्रोलर चाबी बंद होने पर सेटिंग्स को फ्लैश मेमोरी में सुरक्षित रखता है।"
                                else
                                    "Tests whether your controller hardware persists settings across physical ignition key power-cycles.",
                                fontSize = 12.sp,
                                color = YakuzaTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Step 1: Baseline
                item {
                    TestStepCard(
                        stepNum = "1",
                        title = if (language == Language.HINDI) "मूल स्थिति पढ़ें (Baseline Read)" else "Read Current Baseline",
                        description = if (language == Language.HINDI)
                            "वर्तमान सक्रिय स्टार्टअप मोड: ${state.activeStartupMode?.labelHi ?: "सॉफ्ट स्टार्ट"}"
                        else
                            "Current Active Startup Mode: ${state.activeStartupMode?.labelEn ?: "Soft Start"}",
                        actionLabel = if (language == Language.HINDI) "मूल स्थिति रिकॉर्ड करें" else "Record Baseline",
                        isCompleted = testStep.ordinal > PersistenceTestStep.STEP_1_RECORD_BASELINE.ordinal,
                        onAction = {
                            onRecordBaseline(SettingCategory.STARTUP_MODE, state.activeStartupMode)
                            testStep = PersistenceTestStep.STEP_2_MODIFY_PARAMETER
                        }
                    )
                }

                // Step 2: Modify
                item {
                    TestStepCard(
                        stepNum = "2",
                        title = if (language == Language.HINDI) "सेटिंग बदलें (Write Hard Boot)" else "Modify Setting (Hard Boot)",
                        description = if (language == Language.HINDI)
                            "स्टार्टअप मोड को 'Hard Boot' पर सेट करें और बीप की आवाज़ सुनें।"
                        else
                            "Transmit Opcode 0x3E Payload 0x01. Listen for confirmation beep.",
                        actionLabel = if (language == Language.HINDI) "Hard Boot कमांड भेजें" else "Send Hard Boot Command",
                        isCompleted = testStep.ordinal > PersistenceTestStep.STEP_2_MODIFY_PARAMETER.ordinal,
                        enabled = testStep == PersistenceTestStep.STEP_2_MODIFY_PARAMETER,
                        onAction = {
                            onUpdateSetting(SettingCategory.STARTUP_MODE, StartupMode.HARD_BOOT)
                            onRecordWritten(SettingCategory.STARTUP_MODE, StartupMode.HARD_BOOT, true, true)
                            testStep = PersistenceTestStep.STEP_3_POWER_OFF_WAIT
                        }
                    )
                }

                // Step 3: Key Power Cycle
                item {
                    TestStepCard(
                        stepNum = "3",
                        title = if (language == Language.HINDI) "चाबी बंद करें और 15 सेकंड प्रतीक्षा करें" else "Turn Key OFF & Wait 15 Seconds",
                        description = if (language == Language.HINDI)
                            "स्कूटर की चाबी बंद करें ताकि कैपेसिटर डिस्चार्ज हो जाएं, फिर दोबारा चालू करें।"
                        else
                            "Turn ignition switch OFF. Wait 15 seconds for MCU capacitors to fully discharge, then turn switch back ON.",
                        actionLabel = if (state.isSimulated) {
                            if (language == Language.HINDI) "सिम्युलेटेड पावर-साइकिल चलाएं" else "Simulate Power Cycle"
                        } else {
                            if (language == Language.HINDI) "चाबी चालू कर ली (आगे बढ़ें)" else "I Have Cycled Ignition Key"
                        },
                        isCompleted = testStep.ordinal > PersistenceTestStep.STEP_3_POWER_OFF_WAIT.ordinal,
                        enabled = testStep == PersistenceTestStep.STEP_3_POWER_OFF_WAIT,
                        onAction = {
                            if (state.isSimulated) {
                                onTriggerSimulatedPowerCycle()
                            }
                            testStep = PersistenceTestStep.STEP_4_RECONNECT_VERIFY
                        }
                    )
                }

                // Step 4: Reconnect & Verify
                item {
                    TestStepCard(
                        stepNum = "4",
                        title = if (language == Language.HINDI) "पुनः कनेक्ट करके जांचें (Verify Read-back)" else "Reconnect & Verify Read-back",
                        description = if (language == Language.HINDI)
                            "कंट्रोलर से ताज़ा डेटा पढ़कर देखें कि क्या Hard Boot बचा रहा या Soft Start वापस आ गया।"
                        else
                            "Query controller to verify if Hard Boot persisted or reverted to RAM default.",
                        actionLabel = if (language == Language.HINDI) "सत्यापन पूरा करें" else "Evaluate Persistence",
                        isCompleted = testStep == PersistenceTestStep.EVALUATION_COMPLETE,
                        enabled = testStep == PersistenceTestStep.STEP_4_RECONNECT_VERIFY,
                        onAction = {
                            val active = state.activeStartupMode ?: StartupMode.SOFT_START
                            onVerifyPostPowerCycle(SettingCategory.STARTUP_MODE, active)
                            testStep = PersistenceTestStep.EVALUATION_COMPLETE
                        }
                    )
                }

                // Evaluation Results Card
                if (persistenceRecords.isNotEmpty()) {
                    item {
                        persistenceRecords.firstOrNull()?.let { record ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (record.persisted == true) YakuzaStatusConnected.copy(alpha = 0.15f)
                                        else YakuzaStatusDisconnected.copy(alpha = 0.15f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (record.persisted == true) YakuzaStatusConnected else YakuzaStatusDisconnected,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (record.persisted == true) "PERSISTENCE RESULT: PASS" else "PERSISTENCE RESULT: REVERTED",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (record.persisted == true) YakuzaStatusConnected else YakuzaStatusDisconnected
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (language == Language.HINDI) record.resultSummaryHi else record.resultSummaryEn,
                                        fontSize = 13.sp,
                                        color = YakuzaTextPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Packet Stream Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RX: ${state.packetsReceivedCount} | TX: ${state.packetsTransmittedCount}",
                        fontSize = 12.sp,
                        color = YakuzaOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            val text = DiagnosticLogger.exportAsText()
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YakuzaSurfaceElevated)
                    ) {
                        Text(if (language == Language.HINDI) "लॉग कॉपी करें" else "Copy Log", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs.reversed()) { log ->
                        PacketLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun TestStepCard(
    stepNum: String,
    title: String,
    description: String,
    actionLabel: String,
    isCompleted: Boolean,
    enabled: Boolean = true,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isCompleted) YakuzaStatusConnected.copy(alpha = 0.5f) else YakuzaCardBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "STEP $stepNum",
                    color = if (isCompleted) YakuzaStatusConnected else YakuzaOrangePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                if (isCompleted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✓ DONE", color = YakuzaStatusConnected, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YakuzaTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = YakuzaTextSecondary, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAction,
                enabled = enabled && !isCompleted,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YakuzaOrangePrimary)
            ) {
                Text(actionLabel, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PacketLogItem(log: DiagnosticLogEntry) {
    val color = when (log.level) {
        LogLevel.PACKET_TX -> YakuzaOrangePrimary
        LogLevel.PACKET_RX -> YakuzaStatusConnected
        LogLevel.ERROR -> YakuzaStatusDisconnected
        LogLevel.WARN -> YakuzaStatusWarning
        else -> YakuzaTextSecondary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "[${log.formattedTime}] [${log.level}]", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = log.tag, color = YakuzaTextMuted, fontSize = 10.sp)
        }
        Text(text = log.message, color = YakuzaTextPrimary, fontSize = 11.sp)
        log.hexString?.let { hex ->
            Text(
                text = "HEX: $hex",
                color = YakuzaOrangeGlow,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
