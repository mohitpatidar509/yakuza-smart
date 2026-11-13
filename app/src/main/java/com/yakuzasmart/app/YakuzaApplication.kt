package com.yakuzasmart.app

import android.app.Application
import android.content.Intent
import android.util.Log

class YakuzaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("YakuzaSmart", "YakuzaApplication initialized.")

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = java.io.StringWriter()
            val pw = java.io.PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()
            Log.e("YakuzaSmart", "FATAL UNCAUGHT CRASH:\n$stackTrace")

            try {
                val intent = Intent(this, CrashReportActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("EXTRA_CRASH_INFO", "${throwable.javaClass.name}: ${throwable.message}\n\n$stackTrace")
                }
                startActivity(intent)
            } catch (e: Throwable) {
                Log.e("YakuzaSmart", "Failed to launch CrashReportActivity: ${e.message}")
                defaultHandler?.uncaughtException(thread, throwable)
            }

            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }
    }
}
