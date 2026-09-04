package com.cbo.core.logger.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.core.graphics.drawable.IconCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import com.cbo.core.logger.R
import com.cbo.core.logger.ui.LogViewerActivity

/**
 * Registers dynamic App Shortcut for non-release / debuggable variants.
 * Displays "App Logs" shortcut when user long-presses the app launcher icon.
 */
object LogShortcutHelper {

    private const val SHORTCUT_ID_LOGS = "shortcut_app_logs"

    fun setupLogShortcut(context: Context) {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggable) return

        try {
            val intent = Intent(context, LogViewerActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_LOGS)
                .setShortLabel("App Logs")
                .setLongLabel("View & Export Logs")
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_logs))
                .setIntent(intent)
                .build()

            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        } catch (e: Exception) {
            android.util.Log.e("LogShortcutHelper", "Failed to register log shortcut", e)
        }
    }
}
