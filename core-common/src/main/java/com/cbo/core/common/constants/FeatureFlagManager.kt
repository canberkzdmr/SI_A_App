package com.cbo.core.common.constants

object FeatureFlagManager {
    /**
     * Konum bazlı hatırlatıcıların arkaplan takibini açıp kapatır.
     * Google Play politikalarında bir sorunla karşılaşırsanız bu değeri false yapıp,
     * AndroidManifest.xml dosyasından ACCESS_BACKGROUND_LOCATION iznini kaldırın.
     */
    const val ENABLE_BACKGROUND_LOCATION = true
}
