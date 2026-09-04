package com.cbo.notes.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cbo.core.common.constants.FeatureFlagManager
import com.cbo.core.logger.AppLogger
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Coğrafi sınır (geofence) ekler. Kullanıcı bu alana girdiğinde tetiklenir.
     */
    @SuppressLint("MissingPermission")
    fun addLocationReminder(
        noteId: Int,
        latitude: Double,
        longitude: Double,
        radiusInMeters: Float = 100f
    ) {
        if (!FeatureFlagManager.ENABLE_BACKGROUND_LOCATION) {
            AppLogger.w("Background location feature is disabled via FeatureFlagManager.")
            return
        }

        val geofence = Geofence.Builder()
            .setRequestId(noteId.toString())
            .setCircularRegion(latitude, longitude, radiusInMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                AppLogger.d("Geofence added for note $noteId")
            }
            addOnFailureListener {
                AppLogger.e("Failed to add geofence for note $noteId", it)
            }
        }
    }

    fun removeLocationReminder(noteId: Int) {
        geofencingClient.removeGeofences(listOf(noteId.toString())).run {
            addOnSuccessListener {
                AppLogger.d("Geofence removed for note $noteId")
            }
            addOnFailureListener {
                AppLogger.e("Failed to remove geofence for note $noteId", it)
            }
        }
        try {
            androidx.core.app.NotificationManagerCompat.from(context).cancel(noteId)
        } catch (e: Exception) {
            AppLogger.e("Failed to cancel notification for note $noteId", e)
        }
    }
}
