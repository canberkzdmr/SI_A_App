package com.cbo.core.data.biometric

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat


object BiometricUtils {

    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFail(activity.getString(com.cbo.core.data.R.string.authentication_failed))
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(com.cbo.core.data.R.string.biometric_login_title))
            .setSubtitle(activity.getString(com.cbo.core.data.R.string.biometric_login_subtitle))
            .setNegativeButtonText(activity.getString(com.cbo.core.data.R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}