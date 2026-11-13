package com.yakuzasmart.app

import android.app.Application
import android.util.Log

class YakuzaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("YakuzaSmart", "YakuzaApplication initialized successfully.")
    }
}
