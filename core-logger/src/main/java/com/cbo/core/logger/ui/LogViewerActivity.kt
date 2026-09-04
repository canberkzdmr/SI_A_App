package com.cbo.core.logger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.cbo.core.logger.AppLogger
import com.cbo.core.logger.database.LogDatabase

class LogViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = LogDatabase.getInstance(this)
        val pruningManager = AppLogger.getPruningManager()

        setContent {
            MaterialTheme {
                LogViewerScreen(
                    database = database,
                    pruningManager = pruningManager,
                    onClose = { finish() }
                )
            }
        }
    }
}
