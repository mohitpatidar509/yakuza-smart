package com.yakuzasmart.app.core.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR, PACKET_TX, PACKET_RX
}

data class DiagnosticLogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val rawBytes: ByteArray? = null
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))

    val hexString: String?
        get() = rawBytes?.joinToString(separator = " ") { "%02X".format(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DiagnosticLogEntry
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

object DiagnosticLogger {
    private const val MAX_LOGS = 500
    private val _logs = MutableStateFlow<List<DiagnosticLogEntry>>(emptyList())
    val logs: StateFlow<List<DiagnosticLogEntry>> = _logs.asStateFlow()

    fun log(level: LogLevel, tag: String, message: String, rawBytes: ByteArray? = null) {
        val entry = DiagnosticLogEntry(
            level = level,
            tag = tag,
            message = message,
            rawBytes = rawBytes
        )
        val current = _logs.value.toMutableList()
        if (current.size >= MAX_LOGS) {
            current.removeAt(0)
        }
        current.add(entry)
        _logs.value = current
    }

    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARN, tag, message)
    fun e(tag: String, message: String) = log(LogLevel.ERROR, tag, message)
    fun tx(tag: String, message: String, bytes: ByteArray) = log(LogLevel.PACKET_TX, tag, message, bytes)
    fun rx(tag: String, message: String, bytes: ByteArray) = log(LogLevel.PACKET_RX, tag, message, bytes)

    fun clear() {
        _logs.value = emptyList()
    }

    fun exportAsText(): String {
        return _logs.value.joinToString(separator = "\n") { entry ->
            val hex = entry.hexString?.let { " [HEX: $it]" } ?: ""
            "[${entry.formattedTime}] [${entry.level}] [${entry.tag}] ${entry.message}$hex"
        }
    }
}
