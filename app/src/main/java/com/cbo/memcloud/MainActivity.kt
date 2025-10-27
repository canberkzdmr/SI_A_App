package com.cbo.memcloud

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.cbo.ui.snackbar.SnackbarHostProvider
import com.cbo.ui.theme.MemCloudApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
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
