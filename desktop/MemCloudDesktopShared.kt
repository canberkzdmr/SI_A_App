@file:Suppress("unused", "ClassName")

package com.cbo.desktop.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Desktop export bundle
 *
 * This file is a "copy bundle" of shared, platform-agnostic code from the Android project
 * to bootstrap a desktop implementation.
 *
 * Sources (original paths in this repo):
 * - core-common/src/main/java/com/cbo/core/common/constants/PreferenceKeys.kt
 * - core-domain/src/main/java/com/cbo/core/domain/model/*.kt
 * - feature/notes/src/main/java/com/cbo/notes/domain/model/*.kt
 * - feature/login/src/main/java/com/cbo/login/domain/model/RegisterUserModel.kt
 * - ui/src/main/java/com/cbo/ui/theme/{Color.kt,Type.kt,Theme.kt} (Android-only system bar code removed)
 */

// region PreferenceKeys (from core-common)

object PreferenceKeys {
    const val PREFS_FILE = "apps_prefs"

    const val LAST_USERNAME = "last_username"
    const val REMEMBER_ME = "remember_me"
    const val DARK_THEME_ENABLED = "dark_theme_enabled"
}

// endregion

// region Notes domain models (from feature:notes)

data class Note(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val category: Category? = null,
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val reminderTime: Long? = null
) {
    /** Returns true if this note has an active reminder set for the future */
    fun hasActiveReminder(): Boolean = reminderTime != null && reminderTime > System.currentTimeMillis()

    /** Returns true if the reminder time has passed */
    fun isReminderExpired(): Boolean = reminderTime != null && reminderTime <= System.currentTimeMillis()

    companion object {
        /** Duration in milliseconds after which soft-deleted notes are permanently removed (7 days) */
        const val DELETION_RETENTION_PERIOD_MS = 7L * 24 * 60 * 60 * 1000

        /** Calculates the timestamp before which deleted notes should be permanently removed */
        fun getExpirationTimestamp(): Long = System.currentTimeMillis() - DELETION_RETENTION_PERIOD_MS
    }

    /** Returns the remaining time in milliseconds before this note is permanently deleted */
    fun getRemainingDeletionTime(): Long? {
        return deletedAt?.let { deleted ->
            val expirationTime = deleted + DELETION_RETENTION_PERIOD_MS
            val remaining = expirationTime - System.currentTimeMillis()
            if (remaining > 0) remaining else 0
        }
    }

    /** Returns the number of days remaining before permanent deletion */
    fun getDaysUntilPermanentDeletion(): Int? {
        return getRemainingDeletionTime()?.let { remaining ->
            (remaining / (24 * 60 * 60 * 1000)).toInt()
        }
    }
}

data class Category(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val color: String? = null,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val notesCount: Int = 0
)

data class Tag(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0
)

// endregion

// region User / Settings domain models (from core-domain + feature:login)

/**
 * Domain model representing user gender.
 * This enum is used across the domain layer for type-safe gender representation.
 */
enum class Gender {
    MALE,
    FEMALE;

    companion object {
        /**
         * Converts a string representation to Gender enum.
         * Returns null if the string doesn't match any Gender value.
         */
        fun fromString(value: String?): Gender? {
            return value?.let {
                try {
                    valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}

/**
 * View mode for displaying notes in different layouts
 */
enum class ViewMode {
    LIST, GRID, COMPACT
}

data class SupportedLanguage(
    val id: Int,
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val isActive: Boolean = false,
)

/**
 * Domain model representing user detail information.
 * This model abstracts away database-specific representations and provides
 * a clean interface for the presentation layer.
 */
data class UserDetail(
    val id: Int?,
    val userId: Int,
    val fullName: String?,
    val avatarUrl: String?,
    val phoneNumber: String?,
    val address: String?,
    val bio: String?,
    val dateOfBirth: Long?, // Stored as epoch millis for easy date picker integration
    val gender: Gender?,
)

data class UserSettings(
    val userId: Int,
    val isFirstLoginDone: Boolean = false,
    val isBiometricsEnabled: Boolean = false,
    val preferredLanguage: String = "en",
)

/**
 * Domain model representing a user with their complete information.
 * This aggregates User, UserDetail, and UserSettings into a single cohesive model
 * for use in the presentation layer.
 */
data class UserWithDetail(
    val user: User,
    val userDetail: UserDetail?,
    val userSettings: UserSettings,
)

data class PasswordVerifyModel(
    val passwordHash: ByteArray,
    val salt: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasswordVerifyModel

        if (!passwordHash.contentEquals(other.passwordHash)) return false
        if (!salt.contentEquals(other.salt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = passwordHash.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}

/**
 * Registration form model (from feature:login).
 *
 * Note: this is intended for UI input; do not persist the raw password fields.
 */
data class RegisterUserModel(
    val id: Int,
    val username: String,
    val password: String,
    val retypePassword: String,
    val email: String,
    val lastPasswordChangeDate: String,
    val registerDate: String,
    val termsAndConditionsChecked: Boolean,
)

// endregion

// region Theme (from ui/theme) — Android-specific system bar code removed for desktop portability

// ---- Color.kt copy

val primaryLight = Color(0xFF2A6A47)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFAFF2C4)
val onPrimaryContainerLight = Color(0xFF0B5130)
val secondaryLight = Color(0xFF4E6354)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFD1E8D5)
val onSecondaryContainerLight = Color(0xFF374B3D)
val tertiaryLight = Color(0xFF3B6470)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFBFE9F8)
val onTertiaryContainerLight = Color(0xFF224C58)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFF6FBF4)
val onBackgroundLight = Color(0xFF181D19)
val surfaceLight = Color(0xFFF6FBF4)
val onSurfaceLight = Color(0xFF181D19)
val surfaceVariantLight = Color(0xFFDCE5DB)
val onSurfaceVariantLight = Color(0xFF414942)
val outlineLight = Color(0xFF717972)
val outlineVariantLight = Color(0xFFC0C9C0)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2C322D)
val inverseOnSurfaceLight = Color(0xFFEDF2EB)
val inversePrimaryLight = Color(0xFF93D5AA)
val surfaceDimLight = Color(0xFFD6DBD5)
val surfaceBrightLight = Color(0xFFF6FBF4)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF0F5EE)
val surfaceContainerLight = Color(0xFFEAEFE8)
val surfaceContainerHighLight = Color(0xFFE5EAE3)
val surfaceContainerHighestLight = Color(0xFFDFE4DD)

val primaryLightMediumContrast = Color(0xFF003F23)
val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
val primaryContainerLightMediumContrast = Color(0xFF3A7954)
val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val secondaryLightMediumContrast = Color(0xFF273A2D)
val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
val secondaryContainerLightMediumContrast = Color(0xFF5D7263)
val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryLightMediumContrast = Color(0xFF0C3B47)
val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightMediumContrast = Color(0xFF4A737F)
val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val errorLightMediumContrast = Color(0xFF740006)
val onErrorLightMediumContrast = Color(0xFFFFFFFF)
val errorContainerLightMediumContrast = Color(0xFFCF2C27)
val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
val backgroundLightMediumContrast = Color(0xFFF6FBF4)
val onBackgroundLightMediumContrast = Color(0xFF181D19)
val surfaceLightMediumContrast = Color(0xFFF6FBF4)
val onSurfaceLightMediumContrast = Color(0xFF0D120F)
val surfaceVariantLightMediumContrast = Color(0xFFDCE5DB)
val onSurfaceVariantLightMediumContrast = Color(0xFF303832)
val outlineLightMediumContrast = Color(0xFF4C544E)
val outlineVariantLightMediumContrast = Color(0xFF676F68)
val scrimLightMediumContrast = Color(0xFF000000)
val inverseSurfaceLightMediumContrast = Color(0xFF2C322D)
val inverseOnSurfaceLightMediumContrast = Color(0xFFEDF2EB)
val inversePrimaryLightMediumContrast = Color(0xFF93D5AA)
val surfaceDimLightMediumContrast = Color(0xFFC3C8C1)
val surfaceBrightLightMediumContrast = Color(0xFFF6FBF4)
val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightMediumContrast = Color(0xFFF0F5EE)
val surfaceContainerLightMediumContrast = Color(0xFFE5EAE3)
val surfaceContainerHighLightMediumContrast = Color(0xFFD9DED7)
val surfaceContainerHighestLightMediumContrast = Color(0xFFCED3CC)

val primaryLightHighContrast = Color(0xFF00341C)
val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
val primaryContainerLightHighContrast = Color(0xFF0F5433)
val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
val secondaryLightHighContrast = Color(0xFF1D3024)
val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
val secondaryContainerLightHighContrast = Color(0xFF3A4E40)
val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
val tertiaryLightHighContrast = Color(0xFF00313C)
val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightHighContrast = Color(0xFF244F5A)
val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
val errorLightHighContrast = Color(0xFF600004)
val onErrorLightHighContrast = Color(0xFFFFFFFF)
val errorContainerLightHighContrast = Color(0xFF98000A)
val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
val backgroundLightHighContrast = Color(0xFFF6FBF4)
val onBackgroundLightHighContrast = Color(0xFF181D19)
val surfaceLightHighContrast = Color(0xFFF6FBF4)
val onSurfaceLightHighContrast = Color(0xFF000000)
val surfaceVariantLightHighContrast = Color(0xFFDCE5DB)
val onSurfaceVariantLightHighContrast = Color(0xFF000000)
val outlineLightHighContrast = Color(0xFF262E28)
val outlineVariantLightHighContrast = Color(0xFF434B44)
val scrimLightHighContrast = Color(0xFF000000)
val inverseSurfaceLightHighContrast = Color(0xFF2C322D)
val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
val inversePrimaryLightHighContrast = Color(0xFF93D5AA)
val surfaceDimLightHighContrast = Color(0xFFB5BAB4)
val surfaceBrightLightHighContrast = Color(0xFFF6FBF4)
val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightHighContrast = Color(0xFFEDF2EB)
val surfaceContainerLightHighContrast = Color(0xFFDFE4DD)
val surfaceContainerHighLightHighContrast = Color(0xFFD1D6CF)
val surfaceContainerHighestLightHighContrast = Color(0xFFC3C8C1)

val primaryDark = Color(0xFF93D5AA)
val onPrimaryDark = Color(0xFF00391F)
val primaryContainerDark = Color(0xFF0B5130)
val onPrimaryContainerDark = Color(0xFFAFF2C4)
val secondaryDark = Color(0xFFB5CCBA)
val onSecondaryDark = Color(0xFF213528)
val secondaryContainerDark = Color(0xFF374B3D)
val onSecondaryContainerDark = Color(0xFFD1E8D5)
val tertiaryDark = Color(0xFFA3CDDB)
val onTertiaryDark = Color(0xFF033641)
val tertiaryContainerDark = Color(0xFF224C58)
val onTertiaryContainerDark = Color(0xFFBFE9F8)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF000000)
val onBackgroundDark = Color(0xFFDFE4DD)
val surfaceDark = Color(0xFF000000)
val onSurfaceDark = Color(0xFFDFE4DD)
val surfaceVariantDark = Color(0xFF414942)
val onSurfaceVariantDark = Color(0xFFC0C9C0)
val outlineDark = Color(0xFF8A938B)
val outlineVariantDark = Color(0xFF414942)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFDFE4DD)
val inverseOnSurfaceDark = Color(0xFF2C322D)
val inversePrimaryDark = Color(0xFF2A6A47)
val surfaceDimDark = Color(0xFF000000)
val surfaceBrightDark = Color(0xFF353B36)
val surfaceContainerLowestDark = Color(0xFF000000)
val surfaceContainerLowDark = Color(0xFF181D19)
val surfaceContainerDark = Color(0xFF1C211D)
val surfaceContainerHighDark = Color(0xFF262B27)
val surfaceContainerHighestDark = Color(0xFF313631)

val primaryDarkMediumContrast = Color(0xFFA9EBBF)
val onPrimaryDarkMediumContrast = Color(0xFF002C17)
val primaryContainerDarkMediumContrast = Color(0xFF5E9E76)
val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
val secondaryDarkMediumContrast = Color(0xFFCBE2CF)
val onSecondaryDarkMediumContrast = Color(0xFF162A1D)
val secondaryContainerDarkMediumContrast = Color(0xFF809685)
val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
val tertiaryDarkMediumContrast = Color(0xFFB9E3F1)
val onTertiaryDarkMediumContrast = Color(0xFF002A34)
val tertiaryContainerDarkMediumContrast = Color(0xFF6E97A4)
val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
val errorDarkMediumContrast = Color(0xFFFFD2CC)
val onErrorDarkMediumContrast = Color(0xFF540003)
val errorContainerDarkMediumContrast = Color(0xFFFF5449)
val onErrorContainerDarkMediumContrast = Color(0xFF000000)
val backgroundDarkMediumContrast = Color(0xFF000000)
val onBackgroundDarkMediumContrast = Color(0xFFDFE4DD)
val surfaceDarkMediumContrast = Color(0xFF000000)
val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkMediumContrast = Color(0xFF414942)
val onSurfaceVariantDarkMediumContrast = Color(0xFFD6DFD5)
val outlineDarkMediumContrast = Color(0xFFACB4AC)
val outlineVariantDarkMediumContrast = Color(0xFF8A928A)
val scrimDarkMediumContrast = Color(0xFF000000)
val inverseSurfaceDarkMediumContrast = Color(0xFFDFE4DD)
val inverseOnSurfaceDarkMediumContrast = Color(0xFF262B27)
val inversePrimaryDarkMediumContrast = Color(0xFF0D5332)
val surfaceDimDarkMediumContrast = Color(0xFF000000)
val surfaceBrightDarkMediumContrast = Color(0xFF404641)
val surfaceContainerLowestDarkMediumContrast = Color(0xFF000000)
val surfaceContainerLowDarkMediumContrast = Color(0xFF1A1F1B)
val surfaceContainerDarkMediumContrast = Color(0xFF242925)
val surfaceContainerHighDarkMediumContrast = Color(0xFF2E342F)
val surfaceContainerHighestDarkMediumContrast = Color(0xFF3A3F3A)

val primaryDarkHighContrast = Color(0xFFBDFFD2)
val onPrimaryDarkHighContrast = Color(0xFF000000)
val primaryContainerDarkHighContrast = Color(0xFF8FD1A6)
val onPrimaryContainerDarkHighContrast = Color(0xFF000F05)
val secondaryDarkHighContrast = Color(0xFFDEF6E3)
val onSecondaryDarkHighContrast = Color(0xFF000000)
val secondaryContainerDarkHighContrast = Color(0xFFB1C8B6)
val onSecondaryContainerDarkHighContrast = Color(0xFF000F05)
val tertiaryDarkHighContrast = Color(0xFFD8F5FF)
val onTertiaryDarkHighContrast = Color(0xFF000000)
val tertiaryContainerDarkHighContrast = Color(0xFF9FC9D7)
val onTertiaryContainerDarkHighContrast = Color(0xFF000D12)
val errorDarkHighContrast = Color(0xFFFFECE9)
val onErrorDarkHighContrast = Color(0xFF000000)
val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
val onErrorContainerDarkHighContrast = Color(0xFF220001)
val backgroundDarkHighContrast = Color(0xFF000000)
val onBackgroundDarkHighContrast = Color(0xFFDFE4DD)
val surfaceDarkHighContrast = Color(0xFF000000)
val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkHighContrast = Color(0xFF414942)
val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
val outlineDarkHighContrast = Color(0xFFEAF2E9)
val outlineVariantDarkHighContrast = Color(0xFFBCC5BC)
val scrimDarkHighContrast = Color(0xFF000000)
val inverseSurfaceDarkHighContrast = Color(0xFFDFE4DD)
val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
val inversePrimaryDarkHighContrast = Color(0xFF0D5332)
val surfaceDimDarkHighContrast = Color(0xFF000000)
val surfaceBrightDarkHighContrast = Color(0xFF4C514C)
val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
val surfaceContainerLowDarkHighContrast = Color(0xFF1C211D)
val surfaceContainerDarkHighContrast = Color(0xFF2C322D)
val surfaceContainerHighDarkHighContrast = Color(0xFF373D38)
val surfaceContainerHighestDarkHighContrast = Color(0xFF434843)

// ---- Type.kt copy

val MemCloudTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

// ---- Theme.kt (portable)

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

/**
 * Provides the currently-applied app theme (system or user override) to child composables.
 */
val LocalIsDarkTheme: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

@Composable
fun MemCloudApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkScheme else lightScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MemCloudTypography
        ) {
            content()
        }
    }
}

// endregion




