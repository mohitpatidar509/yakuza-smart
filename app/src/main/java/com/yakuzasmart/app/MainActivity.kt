package com.yakuzasmart.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.yakuzasmart.app.core.theme.YakuzaSmartTheme
import com.yakuzasmart.app.data.bluetooth.BleManager
import com.yakuzasmart.app.data.bluetooth.BleSimulator
import com.yakuzasmart.app.data.local.AppPreferences
import com.yakuzasmart.app.data.local.SettingsCache
import com.yakuzasmart.app.data.repository.ScooterRepositoryImpl
import com.yakuzasmart.app.data.repository.SettingsRepositoryImpl
import com.yakuzasmart.app.presentation.navigation.YakuzaNavGraph
import com.yakuzasmart.app.presentation.viewmodel.ScooterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScooterViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appPreferences = AppPreferences(applicationContext)
                val bleManager = BleManager(applicationContext, lifecycleScope)
                val bleSimulator = BleSimulator(lifecycleScope)
                val scooterRepo = ScooterRepositoryImpl(
                    bleManager = bleManager,
                    bleSimulator = bleSimulator,
                    appPreferences = appPreferences,
                    scope = lifecycleScope
                )
                val settingsCache = SettingsCache()
                val settingsRepo = SettingsRepositoryImpl(
                    scooterRepositoryImpl = scooterRepo,
                    settingsCache = settingsCache
                )

                return ScooterViewModel(
                    scooterRepository = scooterRepo,
                    settingsRepository = settingsRepo,
                    appPreferences = appPreferences
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YakuzaSmartTheme {
                YakuzaNavGraph(viewModel = viewModel)
            }
        }
    }
}
