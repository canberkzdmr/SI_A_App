package com.cbo.core.data.mapper

import com.cbo.core.database.entity.UserSettings
import javax.inject.Inject

class UserSettingsEntityMapper @Inject constructor() {

    fun toDomain(entity: UserSettings): com.cbo.core.domain.model.UserSettings {
        return com.cbo.core.domain.model.UserSettings(
            userId = entity.userId,
            isFirstLoginDone = entity.isFirstLoginDone,
            isBiometricsEnabled = entity.isBiometricsEnabled
        )
    }

    fun toEntity(domain: com.cbo.core.domain.model.UserSettings): UserSettings {
        return UserSettings(
            userId = domain.userId,
            isBiometricsEnabled = domain.isBiometricsEnabled,
            isFirstLoginDone = domain.isFirstLoginDone,
        )
    }
}