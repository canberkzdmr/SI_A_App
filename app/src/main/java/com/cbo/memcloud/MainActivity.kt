package com.cbo.memcloud

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import com.cbo.ui.snackbar.SnackbarHostProvider
import com.cbo.ui.theme.MemCloudApplicationTheme
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display with proper window insets
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MemCloudApplicationTheme {
                val navController = rememberNavController()
                SnackbarHostProvider { padding ->
                    MainNavHost(
                        navController = navController,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}
