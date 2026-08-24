package com.cbo.notes.domain.model

/**
 * Haritada yer arama işlemi sonucunda dönecek genel model.
 * İleride Google Places yerine başka bir API (Mapbox, Nominatim vb.) kullanılırsa
 * UI katmanı sadece bu modeli tanıyacağı için etkilenmeyecek.
 */
data class LocationSearchResult(
    val id: String,
    val primaryText: String, // Örn: "Kadıköy"
    val secondaryText: String, // Örn: "İstanbul, Türkiye"
    val latitude: Double? = null,
    val longitude: Double? = null
)
