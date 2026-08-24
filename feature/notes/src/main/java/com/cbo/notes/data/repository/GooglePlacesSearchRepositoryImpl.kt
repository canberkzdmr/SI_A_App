package com.cbo.notes.data.repository

import com.cbo.notes.domain.model.LocationSearchResult
import com.cbo.notes.domain.repository.LocationSearchRepository
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GooglePlacesSearchRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient
) : LocationSearchRepository {

    // Arama oturumu boyunca tek bir token kullanmak faturalandırmayı optimize eder
    private var sessionToken: AutocompleteSessionToken? = null

    override suspend fun searchLocation(query: String): Result<List<LocationSearchResult>> {
        if (query.isBlank()) return Result.success(emptyList())

        if (sessionToken == null) {
            sessionToken = AutocompleteSessionToken.newInstance()
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(sessionToken)
            .setQuery(query)
            .build()

        return suspendCancellableCoroutine { continuation ->
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val results = response.autocompletePredictions.map { prediction ->
                        LocationSearchResult(
                            id = prediction.placeId,
                            primaryText = prediction.getPrimaryText(null).toString(),
                            secondaryText = prediction.getSecondaryText(null).toString()
                        )
                    }
                    continuation.resume(Result.success(results))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
                .addOnCanceledListener {
                    if (continuation.isActive) {
                        continuation.resume(Result.success(emptyList()))
                    }
                }
        }
    }

    override suspend fun getLocationDetails(placeId: String): Result<LocationSearchResult> {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields)
            .setSessionToken(sessionToken) // Aynı token ile fetch yapmak aramayı sonlandırıp tek oturum sayar
            .build()

        return suspendCancellableCoroutine { continuation ->
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    
                    // İşlem bitti, yeni aramalar için token'ı sıfırla
                    sessionToken = null
                    
                    val result = LocationSearchResult(
                        id = place.id ?: placeId,
                        primaryText = place.name ?: "",
                        secondaryText = place.address ?: "",
                        latitude = place.latLng?.latitude,
                        longitude = place.latLng?.longitude
                    )
                    continuation.resume(Result.success(result))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
                .addOnCanceledListener {
                    if (continuation.isActive) {
                        continuation.resumeWithException(Exception("İşlem iptal edildi"))
                    }
                }
        }
    }
}
