package com.cbo.core.data.mapper

import com.cbo.core.database.entity.UserSettingsEntity
import com.cbo.core.domain.model.UserSettings
import javax.inject.Inject

class UserSettingsEntityMapper @Inject constructor() {

    fun toDomain(entity: UserSettingsEntity): UserSettings {
        return UserSettings(
            userId = entity.userId,
            isFirstLoginDone = entity.isFirstLoginDone,
            isBiometricsEnabled = entity.isBiometricsEnabled
        )
    }

    fun toEntity(domain: UserSettings): UserSettingsEntity {
        return UserSettingsEntity(
            userId = domain.userId,
            isBiometricsEnabled = domain.isBiometricsEnabled,
            isFirstLoginDone = domain.isFirstLoginDone,
        )
    }
}