package com.hasani.messageapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hasani.messageapp.data.repository.SettingsRepository
import com.hasani.messageapp.ui.navigation.NavGraph
import com.hasani.messageapp.ui.theme.MessageAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Force Persian (RTL) locale
        val locale = java.util.Locale("fa")
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        setContent {
            // Observe theme mode from settings
            val themeMode by settingsRepository.themeMode.collectAsState(initial = SettingsRepository.THEME_MODE_SYSTEM)
            val systemInDarkTheme = isSystemInDarkTheme()
            
            // PIN Lock state
            val isPinLockEnabled by settingsRepository.isPinLockEnabled.collectAsState(initial = false)
            val storedPin by settingsRepository.pinCode.collectAsState(initial = null)
            var isUnlocked by remember { mutableStateOf(false) }
            
            // Color Palette state
            val colorPalette by settingsRepository.colorPalette.collectAsState(initial = SettingsRepository.PALETTE_DEFAULT)
            
            // Determine dark theme based on mode
            val darkTheme = when (themeMode) {
                SettingsRepository.THEME_MODE_LIGHT -> false
                SettingsRepository.THEME_MODE_DARK -> true
                SettingsRepository.THEME_MODE_SYSTEM -> systemInDarkTheme
                else -> systemInDarkTheme // Fallback to system
            }
            
            MessageAppTheme(darkTheme = darkTheme, colorPalette = colorPalette) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Show PIN Lock Screen if enabled and not unlocked
                    if (isPinLockEnabled && storedPin != null && !isUnlocked) {
                        com.hasani.messageapp.ui.screens.auth.PinLockScreen(
                            onPinVerified = { isUnlocked = true },
                            storedPin = storedPin!!
                        )
                    } else {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                            NavGraph(navController = navController)
                            
                            val connectivityObserver = remember { com.hasani.messageapp.util.ConnectivityObserver(applicationContext) }
                            val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)
                            
                            com.hasani.messageapp.ui.components.NetworkConnectivityBanner(
                                isConnected = isConnected,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}
