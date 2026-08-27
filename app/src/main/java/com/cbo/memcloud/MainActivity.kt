package com.cbo.memcloud

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.memcloud.presentation.screen.ForceUpdateScreen
import com.cbo.memcloud.MemcloudApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity of the application.
 *
 * Language is applied app-wide via AppCompatDelegate with autoStoreLocales.
 *
 * We use FragmentActivity for biometric authentication support (BiometricPrompt needs FragmentManager).
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display with proper window insets
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            val darkThemeOverride = viewModel.darkThemeOverride.collectAsStateWithLifecycle().value
            val effectiveDarkTheme = darkThemeOverride ?: systemDarkTheme

            MemCloudApplicationTheme(darkTheme = effectiveDarkTheme) {
                val isForceUpdateRequired = viewModel.isForceUpdateRequired.collectAsStateWithLifecycle().value
                
                if (isForceUpdateRequired) {
                    ForceUpdateScreen()
                } else {
                    MemcloudApp()
                }
            }
        }
    }
}
