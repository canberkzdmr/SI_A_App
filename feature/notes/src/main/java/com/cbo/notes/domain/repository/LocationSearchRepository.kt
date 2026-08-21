package com.cbo.notes.domain.repository

import com.cbo.notes.domain.model.LocationSearchResult

/**
 * Haritada yer araması için kullanılacak olan repository interface'i.
 * Bu arayüz sayesinde ileride Google Places, Mapbox veya OpenStreetMap
 * entegrasyonu tamamen şeffaf bir şekilde değiştirilebilir.
 */
interface LocationSearchRepository {
    
    /**
     * Kullanıcı arama çubuğuna metin yazdıkça çağrılır.
     * @param query Kullanıcının yazdığı metin.
     * @return Bulunan yerlerin listesi.
     */
    suspend fun searchLocation(query: String): Result<List<LocationSearchResult>>
    
    /**
     * Kullanıcı listeden bir yere tıkladığında o yerin tam koordinatlarını
     * ve ekstra detaylarını getirmek için kullanılır.
     * Bazı servisler (Google Places Autocomplete gibi) arama sonucunda sadece Place ID döner,
     * koordinatlar için bu ikinci isteğe ihtiyaç duyulur.
     * @param placeId Seçilen yerin benzersiz kimliği.
     * @return Güncellenmiş (enlem ve boylam içeren) konum modeli.
     */
    suspend fun getLocationDetails(placeId: String): Result<LocationSearchResult>
}
