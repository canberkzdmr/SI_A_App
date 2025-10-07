package com.cbo.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.cbo.core.common.constants.PreferenceKeys

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
){
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PreferenceKeys.PREFS_FILE, Context.MODE_PRIVATE)


    // last username
    fun setLastUserName(userName: String) {
        prefs.edit { putString(PreferenceKeys.LAST_USERNAME, userName) }
    }

    fun getLastUserName(): String? {
        return prefs.getString(PreferenceKeys.LAST_USERNAME, null)
    }
}